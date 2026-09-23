package com.driveu.driverapp.controller;

import com.driveu.driverapp.dto.Request.LoginRequest;
import com.driveu.driverapp.dto.Response.LoginResponse;
import com.driveu.driverapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/driver/login")
    public ResponseEntity<LoginResponse> loginDriver(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.loginDriver(request)
        );
    }

    @PostMapping("/passenger/login")
    public ResponseEntity<LoginResponse> loginPassenger(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.loginPassenger(request)
        );
    }

    @PostMapping("/admin/login")
    public ResponseEntity<LoginResponse> loginAdmin(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.loginAdmin(request)
        );
    }
}