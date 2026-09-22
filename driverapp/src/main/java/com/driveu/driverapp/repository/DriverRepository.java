package com.driveu.driverapp.repository;

import com.driveu.driverapp.entities.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    List<Driver> findAll();

    boolean existsByPhoneNo(String phoneNo);

    boolean existsByEmail(String email);

    boolean existsByPhoneNoAndIdNot(String phoneNo, UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumberAndIdNot(String licenseNumber, UUID id);
}