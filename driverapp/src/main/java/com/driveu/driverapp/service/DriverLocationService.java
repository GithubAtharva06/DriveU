package com.driveu.driverapp.service;

import com.driveu.driverapp.dto.Request.LocationUpdateRequest;
import com.driveu.driverapp.dto.Response.DriverLocationResponse;
import com.driveu.driverapp.entities.Driver;
import com.driveu.driverapp.entities.DriverLocation;
import com.driveu.driverapp.repository.DriverLocationRepository;
import com.driveu.driverapp.repository.DriverRepository;
import org.springframework.stereotype.Service;

import com.driveu.driverapp.dto.Response.NearbyDriverResponse;
import java.util.Comparator;
import java.util.List;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DriverLocationService {

    private final DriverLocationRepository driverLocationRepository;
    private final DriverRepository driverRepository;

    public DriverLocationService(
            DriverLocationRepository driverLocationRepository,
            DriverRepository driverRepository
    ) {
        this.driverLocationRepository = driverLocationRepository;
        this.driverRepository = driverRepository;
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

    private DriverLocationResponse mapToResponse(
            DriverLocation driverLocation
    ) {
        DriverLocationResponse response = new DriverLocationResponse();

        response.setLocationId(driverLocation.getLocationId());
        response.setDriverId(driverLocation.getDriver().getId());
        response.setLatitude(driverLocation.getLatitude());
        response.setLongitude(driverLocation.getLongitude());
        response.setAvailable(driverLocation.getAvailable());
        response.setUpdatedAt(driverLocation.getUpdatedAt());

        return response;
    }

    public DriverLocationResponse updateDriverLocation(
            UUID driverId,
            LocationUpdateRequest request
    ) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow();

        DriverLocation driverLocation = driverLocationRepository
                .findByDriverId(driverId)
                .orElseGet(DriverLocation::new);

        driverLocation.setDriver(driver);
        driverLocation.setLatitude(request.getLatitude());
        driverLocation.setLongitude(request.getLongitude());
        driverLocation.setUpdatedAt(LocalDateTime.now());

        if (driverLocation.getAvailable() == null) {
            driverLocation.setAvailable(false);
        }

        DriverLocation savedLocation =
                driverLocationRepository.save(driverLocation);

        return mapToResponse(savedLocation);
    }
    public List<NearbyDriverResponse> findNearbyDrivers(
            Double pickupLatitude,
            Double pickupLongitude
    ) {

        final double radiusInKm = 2.0;

        return driverLocationRepository.findAll()
                .stream()
                .filter(location -> Boolean.TRUE.equals(location.getAvailable()))
                .map(location -> {

                    double distance = calculateDistance(pickupLatitude, pickupLongitude,
                            location.getLatitude(),
                            location.getLongitude());

                    NearbyDriverResponse response = new NearbyDriverResponse();
                    response.setDriverId(location.getDriver().getId());
                    response.setLatitude(location.getLatitude());
                    response.setLongitude(location.getLongitude());
                    response.setDistanceInKm(distance);
                    return response;
                })
                .filter(response ->
                        response.getDistanceInKm() <= radiusInKm
                )
                .sorted(
                        Comparator.comparing(
                                NearbyDriverResponse::getDistanceInKm
                        )
                )
                .toList();
    }

    public DriverLocationResponse updateDriverAvailability(
            UUID driverId,
            Boolean available
    ) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new RuntimeException("Driver not found")
                );

        DriverLocation driverLocation = driverLocationRepository
                .findByDriverId(driverId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Driver must update location before going online"
                        )
                );

        driverLocation.setDriver(driver);
        driverLocation.setAvailable(available);

        DriverLocation savedLocation =
                driverLocationRepository.save(driverLocation);

        return mapToResponse(savedLocation);
    }
}