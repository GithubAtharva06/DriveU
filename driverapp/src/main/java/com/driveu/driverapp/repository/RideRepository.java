package com.driveu.driverapp.repository;

import com.driveu.driverapp.entities.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RideRepository extends JpaRepository<Ride, UUID>{

    List<Ride> findByPassengerId(UUID passengerId);

    List<Ride> findByDriverId(UUID driverId);
}
