package com.driveu.driverapp.service;

import com.driveu.driverapp.dto.Request.LoginRequest;
import com.driveu.driverapp.dto.Response.LoginResponse;
import com.driveu.driverapp.entities.Admin;
import com.driveu.driverapp.entities.Driver;
import com.driveu.driverapp.entities.Passenger;
import com.driveu.driverapp.exception.BusinessException;
import com.driveu.driverapp.repository.AdminRepository;
import com.driveu.driverapp.repository.DriverRepository;
import com.driveu.driverapp.repository.PassengerRepository;
import com.driveu.driverapp.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AdminRepository adminRepository;
    private final DriverRepository driverRepository;
    private final PassengerRepository passengerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AdminRepository adminRepository,
            DriverRepository driverRepository,
            PassengerRepository passengerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.adminRepository = adminRepository;
        this.driverRepository = driverRepository;
        this.passengerRepository = passengerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse loginDriver(LoginRequest request) {

        Driver driver = driverRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BusinessException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                driver.getPassword())) {

            throw new BusinessException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                driver.getId(),
                driver.getEmail(),
                "DRIVER"
        );

        return new LoginResponse(
                driver.getId(),
                driver.getUserName(),
                driver.getEmail(),
                "DRIVER",
                token
        );
    }

    public LoginResponse loginPassenger(LoginRequest request) {

        Passenger passenger = passengerRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BusinessException("Invalid email or password"));

        if (!passenger.isActive()) {
            throw new BusinessException("Invalid email or password");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                passenger.getPassword())) {

            throw new BusinessException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                passenger.getId(),
                passenger.getEmail(),
                "PASSENGER"
        );

        return new LoginResponse(
                passenger.getId(),
                passenger.getUserName(),
                passenger.getEmail(),
                "PASSENGER",
                token
        );
    }

    public LoginResponse loginAdmin(LoginRequest request) {

        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BusinessException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                admin.getPassword())) {

            throw new BusinessException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                admin.getId(),
                admin.getEmail(),
                "ADMIN"
        );

        return new LoginResponse(
                admin.getId(),
                admin.getUserName(),
                admin.getEmail(),
                "ADMIN",
                token
        );
    }
}