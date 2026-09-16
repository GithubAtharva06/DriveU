
package com.driveu.driverapp.controller;

import com.driveu.driverapp.dto.Request.DriverRequest;
import com.driveu.driverapp.dto.Response.DriverResponse;
import com.driveu.driverapp.service.DriverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    // Register a new driver
    @PostMapping
    public ResponseEntity<DriverResponse> registerDriver(
            @RequestBody DriverRequest request) {

        DriverResponse response = driverService.registerDriver(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get driver by ID
    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriverById(
            @PathVariable UUID id) {

        DriverResponse response = driverService.getDriverById(id);

        return ResponseEntity.ok(response);
    }

    // Get all drivers
    @GetMapping
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {

        List<DriverResponse> response = driverService.getAllDrivers();

        return ResponseEntity.ok(response);
    }

    // Update driver
    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable UUID id,
            @RequestBody DriverRequest request) {

        DriverResponse response =
                driverService.updateDriver(id, request);

        return ResponseEntity.ok(response);
    }

    // Deactivate driver account
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateDriver(
            @PathVariable UUID id) {

        driverService.deactivateDriver(id);

        return ResponseEntity.noContent().build();
    }

    // Reactivate driver account
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateDriver(
            @PathVariable UUID id) {

        driverService.reactivateDriver(id);

        return ResponseEntity.noContent().build();
    }

    // Set driver online
    @PatchMapping("/{id}/online")
    public ResponseEntity<Void> setDriverOnline(
            @PathVariable UUID id) {

        driverService.setDriverOnline(id);

        return ResponseEntity.noContent().build();
    }

    // Set driver offline
    @PatchMapping("/{id}/offline")
    public ResponseEntity<Void> setDriverOffline(
            @PathVariable UUID id) {

        driverService.setDriverOffline(id);

        return ResponseEntity.noContent().build();
    }

    // Check driver availability
    @GetMapping("/{id}/availability")
    public ResponseEntity<Boolean> isDriverAvailable(
            @PathVariable UUID id) {

        boolean available = driverService.isDriverAvailable(id);

        return ResponseEntity.ok(available);
    }

    // Permanently delete driver
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(
            @PathVariable UUID id) {

        driverService.deleteDriver(id);

        return ResponseEntity.noContent().build();
    }
}