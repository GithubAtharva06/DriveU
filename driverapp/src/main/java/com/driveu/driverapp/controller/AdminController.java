package com.driveu.driverapp.controller;

import com.driveu.driverapp.dto.Request.AdminRequest;
import com.driveu.driverapp.dto.Response.AdminResponse;
import com.driveu.driverapp.dto.Response.DriverResponse;
import com.driveu.driverapp.dto.Response.PassengerResponse;
import com.driveu.driverapp.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping
    public ResponseEntity<AdminResponse> registerAdmin(
            @RequestBody AdminRequest request) {

        AdminResponse response = adminService.registerAdmin(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminResponse> getAdminById(
            @PathVariable UUID id) {

        AdminResponse response = adminService.getAdminById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {

        List<AdminResponse> response = adminService.getAllAdmins();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminResponse> updateAdmin(
            @PathVariable UUID id,
            @RequestBody AdminRequest request) {

        AdminResponse response = adminService.updateAdmin(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(
            @PathVariable UUID id) {

        adminService.deleteAdmin(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/passengers")
    public ResponseEntity<List<PassengerResponse>> getAllPassengers() {

        List<PassengerResponse> response = adminService.getAllPassengers();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/drivers")
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {

        List<DriverResponse> response = adminService.getAllDrivers();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/passengers/{id}/deactivate")
    public ResponseEntity<Void> deactivatePassenger(
            @PathVariable UUID id) {

        adminService.deactivatePassenger(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/drivers/{id}/deactivate")
    public ResponseEntity<Void> deactivateDriver(
            @PathVariable UUID id) {

        adminService.deactivateDriver(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/passengers/{id}")
    public ResponseEntity<Void> deletePassenger(
            @PathVariable UUID id) {

        adminService.deletePassenger(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/drivers/{id}")
    public ResponseEntity<Void> deleteDriver(
            @PathVariable UUID id) {

        adminService.deleteDriver(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/passengers")
    public ResponseEntity<Void> deleteMultiplePassengers(
            @RequestBody List<UUID> ids) {

        adminService.deleteMultiplePassengers(ids);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/drivers")
    public ResponseEntity<Void> deleteMultipleDrivers(
            @RequestBody List<UUID> ids) {

        adminService.deleteMultipleDrivers(ids);

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/passengers/{id}")
    public ResponseEntity<PassengerResponse> getPassengerById(
            @PathVariable UUID id) {

        PassengerResponse response = adminService.getPassengerById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/drivers/{id}")
    public ResponseEntity<DriverResponse> getDriverById(
            @PathVariable UUID id) {

        DriverResponse response = adminService.getDriverById(id);

        return ResponseEntity.ok(response);
    }
}