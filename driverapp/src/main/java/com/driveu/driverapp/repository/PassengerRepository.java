package com.driveu.driverapp.repository;

import com.driveu.driverapp.entities.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PassengerRepository extends JpaRepository<Passenger, UUID> {

    Optional<Passenger> findByEmail(String email);

    boolean existsByPhoneNo(String phoneNo);

    boolean existsByEmail(String email);

    boolean existsByPhoneNoAndIdNot(String phoneNo, UUID id);

    boolean existsByEmailAndIdNot(String email, UUID id);
}