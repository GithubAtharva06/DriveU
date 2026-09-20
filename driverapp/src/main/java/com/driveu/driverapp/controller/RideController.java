
package com.driveu.driverapp.controller;
import com.driveu.driverapp.dto.Request.RideStatusUpdateRequest;
import com.driveu.driverapp.dto.Response.RideResponse;
import com.driveu.driverapp.service.RideService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.driveu.driverapp.dto.Request.RideRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    // Create a new ride
    @PostMapping
    public ResponseEntity<RideResponse> createRide(
            @Valid @RequestBody RideRequest request
    ) {

        RideResponse response = rideService.createRide(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get ride by ID
    @GetMapping("/{rideId}")
    public ResponseEntity<RideResponse> getRideById(
            @PathVariable UUID rideId
    ) {
        RideResponse response = rideService.getRideById(rideId);

        return ResponseEntity.ok(response);
    }
    // Get all rides
    @GetMapping
    public ResponseEntity<List<RideResponse>> getAllRides() {

        List<RideResponse> responses = rideService.getAllRides();

        return ResponseEntity.ok(responses);
    }

    // Get rides by passenger
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<RideResponse>> getRidesByPassenger(
            @PathVariable UUID passengerId
    ) {
        List<RideResponse> responses =
                rideService.getRidesByPassenger(passengerId);

        return ResponseEntity.ok(responses);
    }

    // Get rides by driver
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<RideResponse>> getRidesByDriver(
            @PathVariable UUID driverId
    ) {
        List<RideResponse> responses =
                rideService.getRidesByDriver(driverId);

        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{rideId}/accept")
    public ResponseEntity<RideResponse> acceptRide(
            @PathVariable UUID rideId,
            @RequestParam UUID driverId
    ) {
        RideResponse response = rideService.acceptRide(rideId, driverId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{rideId}/status")
    public ResponseEntity<RideResponse> updateRideStatus(
            @PathVariable UUID rideId,
            @RequestParam UUID driverId,
            @Valid @RequestBody RideStatusUpdateRequest request
    ) {

        RideResponse response = rideService.updateRideStatus(
                rideId,
                driverId,
                request.getRideStatus()
        );

        return ResponseEntity.ok(response);
    }
}