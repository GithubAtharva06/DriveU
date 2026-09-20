package com.driveu.driverapp.service;

import com.driveu.driverapp.dto.Response.NearbyDriverResponse;
import com.driveu.driverapp.dto.Response.RideOfferResponse;
import com.driveu.driverapp.entities.Driver;
import com.driveu.driverapp.entities.OfferStatus;
import com.driveu.driverapp.entities.Ride;
import com.driveu.driverapp.entities.RideOffer;
import com.driveu.driverapp.entities.RideStatus;
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
                        new RuntimeException("Ride not found")
                );

        if (ride.getRideStatus() != RideStatus.REQUESTED) {
            throw new RuntimeException(
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
                                    new RuntimeException(
                                            "Driver not found"
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

        return rideOfferRepository
                .findByRide_RideId(rideId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET OFFERS FOR A DRIVER

    public List<RideOfferResponse> getOffersByDriver(UUID driverId) {

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

        if (!offer.getDriver().getId().equals(driverId)) {
            throw new RuntimeException(
                    "This offer does not belong to the driver"
            );
        }

        if (offer.getOfferStatus() != OfferStatus.PENDING) {
            throw new RuntimeException(
                    "Only pending offers can be declined"
            );
        }

        if (offer.getExpiresAt().isBefore(LocalDateTime.now())) {

            offer.setOfferStatus(OfferStatus.EXPIRED);

            rideOfferRepository.save(offer);

            throw new RuntimeException(
                    "This offer has expired"
            );
        }

        offer.setOfferStatus(OfferStatus.DECLINED);

        RideOffer savedOffer = rideOfferRepository.save(offer);

        return mapToResponse(savedOffer);
    }

    // ACCEPT OFFER

    @Transactional
    public RideOfferResponse acceptOffer(
            UUID offerId,
            UUID driverId
    ) {

        RideOffer offer = getOffer(offerId);

        if (!offer.getDriver().getId().equals(driverId)) {
            throw new RuntimeException(
                    "This offer does not belong to the driver"
            );
        }

        if (offer.getOfferStatus() != OfferStatus.PENDING) {
            throw new RuntimeException(
                    "Only pending offers can be accepted"
            );
        }

        if (offer.getExpiresAt().isBefore(LocalDateTime.now())) {

            offer.setOfferStatus(OfferStatus.EXPIRED);

            rideOfferRepository.save(offer);

            throw new RuntimeException(
                    "This offer has expired"
            );
        }

        Ride ride = offer.getRide();

        // Ensure only one driver accepts the ride

        if (ride.getRideStatus() != RideStatus.REQUESTED) {
            throw new RuntimeException(
                    "This ride is no longer available"
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
            throw new RuntimeException(
                    "Driver already has an active ride"
            );
        }

        // Assign driver to ride

        ride.setDriver(offer.getDriver());
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

        RideOffer savedOffer = rideOfferRepository.save(offer);

        return mapToResponse(savedOffer);
    }

    // HELPER METHODS

    private RideOffer getOffer(UUID offerId) {

        return rideOfferRepository.findById(offerId)
                .orElseThrow(() ->
                        new RuntimeException("Ride offer not found")
                );
    }

    private RideOfferResponse mapToResponse(RideOffer offer) {

        RideOfferResponse response = new RideOfferResponse();

        response.setOfferId(offer.getOfferId());
        response.setRideId(offer.getRide().getRideId());
        response.setDriverId(offer.getDriver().getId());
        response.setOfferStatus(offer.getOfferStatus());
        response.setCreatedAt(offer.getCreatedAt());
        response.setExpiresAt(offer.getExpiresAt());

        return response;
    }
}