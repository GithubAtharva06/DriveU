package com.driveu.driverapp.service;

import com.driveu.driverapp.dto.Request.LocationUpdateRequest;
import com.driveu.driverapp.dto.Response.DriverLocationResponse;
import com.driveu.driverapp.dto.Response.NearbyDriverResponse;
import com.driveu.driverapp.entities.Driver;
import com.driveu.driverapp.entities.DriverLocation;
import com.driveu.driverapp.entities.StatusCheck;
import com.driveu.driverapp.repository.DriverLocationRepository;
import com.driveu.driverapp.repository.DriverRepository;
import org.springframework.stereotype.Service;

import com.driveu.driverapp.entities.OfferStatus;
import com.driveu.driverapp.entities.RideOffer;
import com.driveu.driverapp.repository.RideOfferRepository;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class DriverLocationService {
    private static final double MAX_OFFER_DISTANCE_KM = 2.0;

    private final RideOfferRepository rideOfferRepository;

    private final DriverLocationRepository driverLocationRepository;
    private final DriverRepository driverRepository;

    public DriverLocationService(
            DriverLocationRepository driverLocationRepository,
            DriverRepository driverRepository,
            RideOfferRepository rideOfferRepository
    ) {
        this.driverLocationRepository = driverLocationRepository;
        this.driverRepository = driverRepository;
        this.rideOfferRepository = rideOfferRepository;
    }

    private double calculateDistance(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2
    ) {

        final double earthRadiusInKm = 6371.0;

        double latitudeDifference =
                Math.toRadians(latitude2 - latitude1);

        double longitudeDifference =
                Math.toRadians(longitude2 - longitude1);

        double a =
                Math.sin(latitudeDifference / 2)
                        * Math.sin(latitudeDifference / 2)
                        + Math.cos(Math.toRadians(latitude1))
                        * Math.cos(Math.toRadians(latitude2))
                        * Math.sin(longitudeDifference / 2)
                        * Math.sin(longitudeDifference / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return earthRadiusInKm * c;
    }

    private boolean isOutsideOfferRange(
            double driverLatitude,
            double driverLongitude,
            double pickupLatitude,
            double pickupLongitude
    ) {
        double distance = calculateDistance(
                driverLatitude,
                driverLongitude,
                pickupLatitude,
                pickupLongitude
        );

        return distance > MAX_OFFER_DISTANCE_KM;
    }
    @Transactional
    public void expireOffersOutsideRange(
            UUID driverId,
            double driverLatitude,
            double driverLongitude
    ) {

        List<RideOffer> pendingOffers =
                rideOfferRepository.findByDriver_IdAndOfferStatus(
                        driverId,
                        OfferStatus.PENDING
                );

        LocalDateTime now = LocalDateTime.now();

        for (RideOffer offer : pendingOffers) {

            // Expire offers that have passed their expiry time
            if (!offer.getExpiresAt().isAfter(now)) {

                offer.setOfferStatus(OfferStatus.EXPIRED);
                continue;
            }

            double pickupLatitude =
                    offer.getRide().getPickupLatitude();

            double pickupLongitude =
                    offer.getRide().getPickupLongitude();

            boolean outsideRange = isOutsideOfferRange(
                    driverLatitude,
                    driverLongitude,
                    pickupLatitude,
                    pickupLongitude
            );

            if (outsideRange) {

                offer.setOfferStatus(OfferStatus.EXPIRED);
            }
        }

        rideOfferRepository.saveAll(pendingOffers);
    }

    private DriverLocationResponse mapToResponse(
            DriverLocation driverLocation
    ) {

        DriverLocationResponse response =
                new DriverLocationResponse();

        response.setLocationId(driverLocation.getLocationId());
        response.setDriverId(driverLocation.getDriver().getId());
        response.setLatitude(driverLocation.getLatitude());
        response.setLongitude(driverLocation.getLongitude());
        response.setUpdatedAt(driverLocation.getUpdatedAt());

        return response;
    }

    @Transactional
    public DriverLocationResponse updateDriverLocation(
            UUID driverId,
            LocationUpdateRequest request
    ) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Driver not found with ID: " + driverId
                        )
                );

        DriverLocation driverLocation =
                driverLocationRepository
                        .findByDriverId(driverId)
                        .orElseGet(DriverLocation::new);

        driverLocation.setDriver(driver);
        driverLocation.setLatitude(request.getLatitude());
        driverLocation.setLongitude(request.getLongitude());
        driverLocation.setUpdatedAt(LocalDateTime.now());

        DriverLocation savedLocation =
                driverLocationRepository.save(driverLocation);

        // Expire pending offers if the driver is outside the pickup range
        expireOffersOutsideRange(
                driverId,
                request.getLatitude(),
                request.getLongitude()
        );

        return mapToResponse(savedLocation);
    }

    public List<NearbyDriverResponse> findNearbyDrivers(
            Double pickupLatitude,
            Double pickupLongitude
    ) {

        final double radiusInKm = 2.0;

        return driverLocationRepository.findAll()
                .stream()

                // Only drivers who are currently ONLINE
                .filter(location ->
                        location.getDriver().getStatus() == StatusCheck.ONLINE
                )

                .map(location -> {

                    double distance = calculateDistance(
                            pickupLatitude,
                            pickupLongitude,
                            location.getLatitude(),
                            location.getLongitude()
                    );

                    NearbyDriverResponse response =
                            new NearbyDriverResponse();

                    response.setDriverId(
                            location.getDriver().getId()
                    );

                    response.setLatitude(
                            location.getLatitude()
                    );

                    response.setLongitude(
                            location.getLongitude()
                    );

                    response.setDistanceInKm(distance);

                    return response;
                })

                // Only drivers within 2 km
                .filter(response ->
                        response.getDistanceInKm() <= radiusInKm
                )

                // Nearest driver first
                .sorted(
                        Comparator.comparing(
                                NearbyDriverResponse::getDistanceInKm
                        )
                )

                .toList();
    }

    public boolean isDriverWithinRange(
            UUID driverId,
            double pickupLatitude,
            double pickupLongitude
    ) {

        DriverLocation driverLocation =
                driverLocationRepository.findByDriverId(driverId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Driver location not found"
                                )
                        );

        double distance = calculateDistance(
                driverLocation.getLatitude(),
                driverLocation.getLongitude(),
                pickupLatitude,
                pickupLongitude
        );

        return distance <= MAX_OFFER_DISTANCE_KM;
    }


}