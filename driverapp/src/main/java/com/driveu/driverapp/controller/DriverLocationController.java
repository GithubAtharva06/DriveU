package com.driveu.driverapp.controller;

import com.driveu.driverapp.dto.Request.LocationUpdateRequest;
import com.driveu.driverapp.dto.Response.DriverLocationResponse;
import com.driveu.driverapp.dto.Response.NearbyDriverResponse;
import com.driveu.driverapp.service.DriverLocationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/locations")
public class DriverLocationController {

    private final DriverLocationService driverLocationService;

    public DriverLocationController(
            DriverLocationService driverLocationService
    ) {
        this.driverLocationService = driverLocationService;
    }

    @PutMapping("/driver/{driverId}")
    public ResponseEntity<DriverLocationResponse> updateDriverLocation(
            @PathVariable UUID driverId,
            @Valid @RequestBody LocationUpdateRequest request
    ) {

        DriverLocationResponse response =
                driverLocationService.updateDriverLocation(
                        driverId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<NearbyDriverResponse>> findNearbyDrivers(
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {

        List<NearbyDriverResponse> response =
                driverLocationService.findNearbyDrivers(
                        latitude,
                        longitude
                );

        return ResponseEntity.ok(response);
    }
}