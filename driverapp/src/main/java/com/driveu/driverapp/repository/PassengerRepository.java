package com.driveu.driverapp.repository;

import com.driveu.driverapp.entities.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PassengerRepository extends JpaRepository<Passenger, UUID> {
}
