package com.driveu.driverapp.service;

import com.driveu.driverapp.dto.Request.LocationUpdateRequest;
import com.driveu.driverapp.dto.Response.DriverLocationResponse;
import com.driveu.driverapp.entities.Driver;
import com.driveu.driverapp.entities.DriverLocation;
import com.driveu.driverapp.repository.DriverLocationRepository;
import com.driveu.driverapp.repository.DriverRepository;
import org.springframework.stereotype.Service;

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
}