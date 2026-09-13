package com.driveu.driverapp.repository;

import com.driveu.driverapp.entities.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RideRepository extends JpaRepository<Ride, UUID>{

}
