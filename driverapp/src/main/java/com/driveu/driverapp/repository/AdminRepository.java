package com.driveu.driverapp.repository;

import com.driveu.driverapp.entities.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdminRepository extends JpaRepository<Admin, UUID> {

    Optional<Admin> findByEmail(String email);

    boolean existsByPhoneNo(String phoneNo);

    boolean existsByEmail(String email);

    boolean existsByPhoneNoAndIdNot(String phoneNo, UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);
}