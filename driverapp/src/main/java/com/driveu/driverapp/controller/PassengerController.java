package com.driveu.driverapp.controller;

import com.driveu.driverapp.dto.Request.PassengerRequest;
import com.driveu.driverapp.dto.Response.PassengerResponse;
import com.driveu.driverapp.service.PassengerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/passengers")
public class PassengerController {

    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @PostMapping
    public ResponseEntity<PassengerResponse> registerPassenger(
            @Valid @RequestBody PassengerRequest request) {

        PassengerResponse response =
                passengerService.registerPassenger(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerResponse> getPassengerById(
            @PathVariable UUID id) {

        PassengerResponse response =
                passengerService.getPassengerById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PassengerResponse>> getAllPassengers() {

        List<PassengerResponse> response =
                passengerService.getAllPassengers();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PassengerResponse> updatePassenger(
            @PathVariable UUID id,
            @Valid @RequestBody PassengerRequest request) {

        PassengerResponse response =
                passengerService.updatePassenger(id, request);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePassenger(
            @PathVariable UUID id) {

        passengerService.deactivatePassenger(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivatePassenger(
            @PathVariable UUID id) {

        passengerService.reactivatePassenger(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePassenger(
            @PathVariable UUID id) {

        passengerService.deletePassenger(id);

        return ResponseEntity.noContent().build();
    }
}