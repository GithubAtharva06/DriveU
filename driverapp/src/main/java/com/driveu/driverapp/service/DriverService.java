
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

    private Driver findDriver(UUID id) {
        return driverRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Driver not found with ID: " + id));
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

        if (driverRepository.existsByPhoneNo(request.getPhoneNo())) {
            throw new RuntimeException("Phone number is already registered");
        }

        if (driverRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new RuntimeException("License number is already registered");
        }

        Driver driver = new Driver();

        driver.setPhoneNo(request.getPhoneNo());
        driver.setUserName(request.getUserName());
        driver.setEmail(request.getEmail());
        driver.setPassword(request.getPassword());
        driver.setLicenseNumber(request.getLicenseNumber());

        driver.setId(UUID.randomUUID());
        driver.setCreatedAt(LocalDateTime.now());

        // Driver entity defaults to OFFLINE.
        driver.setStatus(StatusCheck.OFFLINE);

        driverRepository.save(driver);

        return mapToResponse(driver);
    }

    public DriverResponse getDriverById(UUID id) {
        Driver driver = findDriver(id);

        return mapToResponse(driver);
    }

    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DriverResponse updateDriver(UUID id, DriverRequest request) {

        Driver driver = findDriver(id);

        if (driverRepository.existsByPhoneNoAndIdNot(request.getPhoneNo(), id)) {
            throw new RuntimeException("Phone number is already registered");
        }

        if (driverRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new RuntimeException("Email is already registered");
        }

        if (driverRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new RuntimeException("License number is already registered");
        }

        driver.setPhoneNo(request.getPhoneNo());
        driver.setUserName(request.getUserName());
        driver.setEmail(request.getEmail());
        driver.setPassword(request.getPassword());
        driver.setLicenseNumber(request.getLicenseNumber());

        driverRepository.save(driver);

        return mapToResponse(driver);
    }

    public void deactivateDriver(UUID id) {
        Driver driver = findDriver(id);

        driver.setStatus(StatusCheck.OFFLINE);

        driverRepository.save(driver);
    }

    public void deleteDriver(UUID id) {
        Driver driver = findDriver(id);

        driverRepository.delete(driver);
    }

    public void setDriverOnline(UUID id) {
        Driver driver = findDriver(id);

        driver.setStatus(StatusCheck.ONLINE);

        driverRepository.save(driver);
    }

    public void setDriverOffline(UUID id) {
        Driver driver = findDriver(id);

        driver.setStatus(StatusCheck.OFFLINE);

        driverRepository.save(driver);
    }

    public boolean isDriverAvailable(UUID id) {
        Driver driver = findDriver(id);

        return driver.getStatus() == StatusCheck.ONLINE;
    }
}