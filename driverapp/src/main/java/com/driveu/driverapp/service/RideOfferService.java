package com.driveu.driverapp.service;

import com.driveu.driverapp.dto.Response.NearbyDriverResponse;
import com.driveu.driverapp.dto.Response.RideOfferResponse;
import com.driveu.driverapp.entities.*;
import com.driveu.driverapp.exception.BusinessException;
import com.driveu.driverapp.exception.ResourceNotFoundException;
import com.driveu.driverapp.repository.DriverRepository;
import com.driveu.driverapp.repository.RideOfferRepository;
import com.driveu.driverapp.repository.RideRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RideOfferService {

    private final RideOfferRepository rideOfferRepository;
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final DriverLocationService driverLocationService;

    public RideOfferService(
            RideOfferRepository rideOfferRepository,
            RideRepository rideRepository,
            DriverRepository driverRepository,
            DriverLocationService driverLocationService
    ) {
        this.rideOfferRepository = rideOfferRepository;
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
        this.driverLocationService = driverLocationService;
    }

    // GENERATE OFFERS FOR NEARBY DRIVERS

    @Transactional
    public List<RideOfferResponse> generateOffersForRide(UUID rideId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ride not found with ID: " + rideId
                        )
                );

        if (ride.getRideStatus() != RideStatus.REQUESTED) {
            throw new BusinessException(
                    "Offers can only be generated for requested rides"
            );
        }

        List<NearbyDriverResponse> nearbyDrivers =
                driverLocationService.findNearbyDrivers(
                        ride.getPickupLatitude(),
                        ride.getPickupLongitude()
                );

        LocalDateTime now = LocalDateTime.now();

        List<RideOffer> newOffers = nearbyDrivers.stream()
                .filter(driver ->
                        !rideOfferRepository
                                .existsByRide_RideIdAndDriver_Id(
                                        rideId,
                                        driver.getDriverId()
                                )
                )
                .map(driver -> {

                    Driver assignedDriver =
                            driverRepository.findById(
                                    driver.getDriverId()
                            ).orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Driver not found with ID: "
                                                    + driver.getDriverId()
                                    )
                            );

                    RideOffer offer = new RideOffer();

                    offer.setRide(ride);
                    offer.setDriver(assignedDriver);
                    offer.setOfferStatus(OfferStatus.PENDING);
                    offer.setCreatedAt(now);
                    offer.setExpiresAt(now.plusMinutes(1));

                    return offer;
                })
                .toList();

        List<RideOffer> savedOffers =
                rideOfferRepository.saveAll(newOffers);

        return savedOffers.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET OFFER BY ID

    public RideOfferResponse getOfferById(UUID offerId) {

        RideOffer offer = getOffer(offerId);

        return mapToResponse(offer);
    }

    // GET OFFERS FOR A RIDE

    public List<RideOfferResponse> getOffersByRide(UUID rideId) {

        if (!rideRepository.existsById(rideId)) {
            throw new ResourceNotFoundException(
                    "Ride not found with ID: " + rideId
            );
        }

        return rideOfferRepository
                .findByRide_RideId(rideId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET OFFERS FOR A DRIVER

    public List<RideOfferResponse> getOffersByDriver(UUID driverId) {

        if (!driverRepository.existsById(driverId)) {
            throw new ResourceNotFoundException(
                    "Driver not found with ID: " + driverId
            );
        }

        return rideOfferRepository
                .findByDriver_Id(driverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // DECLINE OFFER

    @Transactional
    public RideOfferResponse declineOffer(
            UUID offerId,
            UUID driverId
    ) {

        RideOffer offer = getOffer(offerId);

        validateOfferOwnership(offer, driverId);

        if (offer.getOfferStatus() != OfferStatus.PENDING) {
            throw new BusinessException(
                    "Only pending offers can be declined"
            );
        }

        expireOfferIfNecessary(offer);

        if (offer.getOfferStatus() == OfferStatus.EXPIRED) {
            throw new BusinessException(
                    "This offer has expired"
            );
        }

        offer.setOfferStatus(OfferStatus.DECLINED);

        RideOffer savedOffer =
                rideOfferRepository.save(offer);

        return mapToResponse(savedOffer);
    }

    // ACCEPT OFFER

    @Transactional
    public RideOfferResponse acceptOffer(
            UUID offerId,
            UUID driverId
    ) {

        RideOffer offer = getOffer(offerId);

        validateOfferOwnership(offer, driverId);

        if (offer.getOfferStatus() != OfferStatus.PENDING) {
            throw new BusinessException(
                    "Only pending offers can be accepted"
            );
        }

        // Check whether the offer has expired by time

        expireOfferIfNecessary(offer);

        if (offer.getOfferStatus() == OfferStatus.EXPIRED) {
            throw new BusinessException(
                    "This offer has expired"
            );
        }

        Ride ride = offer.getRide();

        // Ensure only one driver accepts the ride

        if (ride.getRideStatus() != RideStatus.REQUESTED) {
            throw new BusinessException(
                    "This ride is no longer available"
            );
        }

        Driver driver = offer.getDriver();

        // Driver must be online

        if (driver.getStatus() != StatusCheck.ONLINE) {
            throw new BusinessException(
                    "Driver must be online to accept an offer"
            );
        }

        // Check driver's current distance from pickup location

        boolean withinRange =
                driverLocationService.isDriverWithinRange(
                        driverId,
                        ride.getPickupLatitude(),
                        ride.getPickupLongitude()
                );

        if (!withinRange) {

            offer.setOfferStatus(OfferStatus.EXPIRED);

            rideOfferRepository.save(offer);

            throw new BusinessException(
                    "You are outside the 2 km pickup range"
            );
        }

        // Ensure driver has no active ride

        List<RideStatus> activeStatuses = List.of(
                RideStatus.ACCEPTED,
                RideStatus.ARRIVING,
                RideStatus.ARRIVED,
                RideStatus.IN_PROGRESS
        );

        boolean driverHasActiveRide =
                rideRepository.existsByDriverIdAndRideStatusIn(
                        driverId,
                        activeStatuses
                );

        if (driverHasActiveRide) {
            throw new BusinessException(
                    "Driver already has an active ride"
            );
        }

        // Assign driver to ride

        ride.setDriver(driver);
        ride.setRideStatus(RideStatus.ACCEPTED);

        rideRepository.save(ride);

        // Accept selected offer

        offer.setOfferStatus(OfferStatus.ACCEPTED);

        // Automatically close all other pending offers

        List<RideOffer> pendingOffers =
                rideOfferRepository
                        .findByRide_RideIdAndOfferStatus(
                                ride.getRideId(),
                                OfferStatus.PENDING
                        );

        for (RideOffer otherOffer : pendingOffers) {

            if (!otherOffer.getOfferId().equals(offerId)) {

                otherOffer.setOfferStatus(
                        OfferStatus.RIDE_BOOKED
                );
            }
        }

        rideOfferRepository.saveAll(pendingOffers);

        RideOffer savedOffer =
                rideOfferRepository.save(offer);

        return mapToResponse(savedOffer);
    }

    // HELPER METHODS

    private RideOffer getOffer(UUID offerId) {

        return rideOfferRepository.findById(offerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ride offer not found with ID: " + offerId
                        )
                );
    }

    private void validateOfferOwnership(
            RideOffer offer,
            UUID driverId
    ) {

        if (!offer.getDriver().getId().equals(driverId)) {

            throw new BusinessException(
                    "This offer does not belong to the driver"
            );
        }
    }

    private void expireOfferIfNecessary(
            RideOffer offer
    ) {

        LocalDateTime now = LocalDateTime.now();

        if (!offer.getExpiresAt().isAfter(now)) {

            offer.setOfferStatus(OfferStatus.EXPIRED);

            rideOfferRepository.save(offer);
        }
    }

    private RideOfferResponse mapToResponse(
            RideOffer offer
    ) {

        RideOfferResponse response =
                new RideOfferResponse();

        response.setOfferId(offer.getOfferId());
        response.setRideId(offer.getRide().getRideId());
        response.setDriverId(offer.getDriver().getId());
        response.setOfferStatus(offer.getOfferStatus());
        response.setCreatedAt(offer.getCreatedAt());
        response.setExpiresAt(offer.getExpiresAt());

        return response;
    }
}