package com.driveu.driverapp.service;

import com.driveu.driverapp.dto.Request.AdminRequest;
import com.driveu.driverapp.dto.Response.AdminResponse;
import com.driveu.driverapp.dto.Response.DriverResponse;
import com.driveu.driverapp.dto.Response.PassengerResponse;
import com.driveu.driverapp.entities.Admin;
import com.driveu.driverapp.entities.Driver;
import com.driveu.driverapp.entities.Passenger;
import com.driveu.driverapp.entities.StatusCheck;
import com.driveu.driverapp.exception.DuplicateResourceException;
import com.driveu.driverapp.exception.ResourceNotFoundException;
import com.driveu.driverapp.repository.AdminRepository;
import com.driveu.driverapp.repository.DriverRepository;
import com.driveu.driverapp.repository.PassengerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;

    public AdminService(
            AdminRepository adminRepository,
            PassengerRepository passengerRepository,
            DriverRepository driverRepository) {

        this.adminRepository = adminRepository;
        this.passengerRepository = passengerRepository;
        this.driverRepository = driverRepository;
    }

    private AdminResponse mapToResponse(Admin admin) {
        AdminResponse response = new AdminResponse();

        response.setId(admin.getId());
        response.setPhoneNo(admin.getPhoneNo());
        response.setUserName(admin.getUserName());
        response.setEmail(admin.getEmail());
        response.setCreatedAt(admin.getCreatedAt());

        return response;
    }

    private PassengerResponse mapPassengerToResponse(Passenger passenger) {
        PassengerResponse response = new PassengerResponse();

        response.setId(passenger.getId());
        response.setPhoneNo(passenger.getPhoneNo());
        response.setUserName(passenger.getUserName());
        response.setEmail(passenger.getEmail());
        response.setCreatedAt(passenger.getCreatedAt());

        return response;
    }

    private DriverResponse mapDriverToResponse(Driver driver) {
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

    // ==================== ADMIN ====================

    public AdminResponse registerAdmin(AdminRequest request) {

        if (adminRepository.existsByPhoneNo(request.getPhoneNo())) {
            throw new DuplicateResourceException(
                    "Phone number is already registered"
            );
        }

        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email is already registered"
            );
        }

        Admin admin = new Admin();

        admin.setPhoneNo(request.getPhoneNo());
        admin.setUserName(request.getUserName());
        admin.setEmail(request.getEmail());
        admin.setPassword(request.getPassword());

        admin.setId(UUID.randomUUID());
        admin.setCreatedAt(LocalDateTime.now());

        adminRepository.save(admin);

        return mapToResponse(admin);
    }

    public AdminResponse getAdminById(UUID id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin not found with ID: " + id
                        ));

        return mapToResponse(admin);
    }

    public List<AdminResponse> getAllAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AdminResponse updateAdmin(UUID id, AdminRequest request) {

        Admin admin = adminRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin not found with ID: " + id
                        ));

        if (adminRepository.existsByPhoneNoAndIdNot(
                request.getPhoneNo(), id)) {

            throw new DuplicateResourceException(
                    "Phone number is already registered"
            );
        }

        if (adminRepository.existsByEmailAndIdNot(
                request.getEmail(), id)) {

            throw new DuplicateResourceException(
                    "Email is already registered"
            );
        }

        admin.setPhoneNo(request.getPhoneNo());
        admin.setUserName(request.getUserName());
        admin.setEmail(request.getEmail());
        admin.setPassword(request.getPassword());

        adminRepository.save(admin);

        return mapToResponse(admin);
    }

    public void deleteAdmin(UUID id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin not found with ID: " + id
                        ));

        adminRepository.delete(admin);
    }

    // ==================== PASSENGERS ====================

    public List<PassengerResponse> getAllPassengers() {
        return passengerRepository.findAll()
                .stream()
                .map(this::mapPassengerToResponse)
                .toList();
    }

    public PassengerResponse getPassengerById(UUID id) {
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Passenger not found with ID: " + id
                        ));

        return mapPassengerToResponse(passenger);
    }

    public void deactivatePassenger(UUID id) {
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Passenger not found with ID: " + id
                        ));

        passenger.setActive(false);

        passengerRepository.save(passenger);
    }

    public void deletePassenger(UUID id) {
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Passenger not found with ID: " + id
                        ));

        passengerRepository.delete(passenger);
    }

    // ==================== DRIVERS ====================

    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAll()
                .stream()
                .map(this::mapDriverToResponse)
                .toList();
    }

    public DriverResponse getDriverById(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver not found with ID: " + id
                        ));

        return mapDriverToResponse(driver);
    }

    public void deactivateDriver(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver not found with ID: " + id
                        ));

        driver.setStatus(StatusCheck.OFFLINE);

        driverRepository.save(driver);
    }

    public void deleteDriver(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver not found with ID: " + id
                        ));

        driverRepository.delete(driver);
    }

    @Transactional
    public void deleteMultiplePassengers(List<UUID> ids) {

        List<Passenger> passengers = passengerRepository.findAllById(ids);

        if (passengers.size() != ids.size()) {
            throw new ResourceNotFoundException(
                    "One or more passengers not found"
            );
        }

        passengerRepository.deleteAll(passengers);
    }

    @Transactional
    public void deleteMultipleDrivers(List<UUID> ids) {

        List<Driver> drivers = driverRepository.findAllById(ids);

        if (drivers.size() != ids.size()) {
            throw new ResourceNotFoundException(
                    "One or more drivers not found"
            );
        }

        driverRepository.deleteAll(drivers);
    }
}