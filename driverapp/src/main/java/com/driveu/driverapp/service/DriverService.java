package com.driveu.driverapp.service;

import com.driveu.driverapp.dto.Request.DriverRequest;
import com.driveu.driverapp.dto.Response.DriverResponse;
import com.driveu.driverapp.entities.Driver;
import com.driveu.driverapp.entities.StatusCheck;
import com.driveu.driverapp.repository.DriverRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    private DriverResponse mapToResponse(Driver driver) {
        DriverResponse response = new DriverResponse();

        response.setId(driver.getId());
        response.setPhoneNo(driver.getPhoneNo());
        response.setUserName(driver.getUserName());
        response.setEmail(driver.getEmail());
        response.setLicenseNumber(driver.getLicenseNumber());
        response.setStatus(driver.getStatus());
        response.setCreatedAt(driver.getCreatedAt());

        return response;
    }

    public DriverResponse registerDriver(DriverRequest request) {
        Driver driver = new Driver();

        driver.setPhoneNo(request.getPhoneNo());
        driver.setUserName(request.getUserName());
        driver.setEmail(request.getEmail());
        driver.setPassword(request.getPassword());
        driver.setLicenseNumber(request.getLicenseNumber());

        driver.setId(UUID.randomUUID());
        driver.setCreatedAt(LocalDateTime.now());

        driverRepository.save(driver);

        return mapToResponse(driver);
    }

    public DriverResponse getDriverById(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow();

        return mapToResponse(driver);
    }

    public List<DriverResponse> getAllDrivers() {
        List<Driver> drivers = driverRepository.findAll();

        return drivers.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DriverResponse updateDriver(UUID id, DriverRequest request) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow();

        driver.setPhoneNo(request.getPhoneNo());
        driver.setUserName(request.getUserName());
        driver.setEmail(request.getEmail());
        driver.setPassword(request.getPassword());
        driver.setLicenseNumber(request.getLicenseNumber());

        driverRepository.save(driver);

        return mapToResponse(driver);
    }

    public void deactivateDriver(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow();

        driver.setStatus(StatusCheck.OFFLINE);

        driverRepository.save(driver);
    }
    public void reactivateDriver(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow();

        driver.setStatus(StatusCheck.OFFLINE);

        driverRepository.save(driver);
    }

    public void deleteDriver(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow();

        driverRepository.delete(driver);
    }
    public void setDriverOnline(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow();

        driver.setStatus(StatusCheck.ONLINE);

        driverRepository.save(driver);
    }

    public void setDriverOffline(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow();

        driver.setStatus(StatusCheck.OFFLINE);

        driverRepository.save(driver);
    }

    public boolean isDriverAvailable(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow();

        return driver.getStatus() == StatusCheck.ONLINE;
    }
}