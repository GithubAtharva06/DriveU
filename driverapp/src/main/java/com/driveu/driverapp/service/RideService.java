package com.driveu.driverapp.service;

import com.driveu.driverapp.dto.Response.RideResponse;
import com.driveu.driverapp.entities.Ride;
import com.driveu.driverapp.repository.DriverRepository;
import com.driveu.driverapp.repository.PassengerRepository;
import com.driveu.driverapp.repository.RideRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;

    public RideService(
            RideRepository rideRepository,
            PassengerRepository passengerRepository,
            DriverRepository driverRepository
    ) {
        this.rideRepository = rideRepository;
        this.passengerRepository = passengerRepository;
        this.driverRepository = driverRepository;
    }

    private RideResponse mapToResponse(Ride ride) {
        RideResponse response = new RideResponse();

        response.setRideId(ride.getRideId());
        response.setPassengerId(ride.getPassenger().getId());

        if (ride.getDriver() != null) {
            response.setDriverId(ride.getDriver().getId());
        }

        response.setRideStatus(ride.getRideStatus());
        response.setPickupLocation(ride.getPickupLocation());
        response.setDropLocation(ride.getDropLocation());
        response.setFare(ride.getFare());
        response.setPickupAt(ride.getPickupAt());
        response.setDropOffAt(ride.getDropOffAt());

        return response;
    }

    public RideResponse getRideById(UUID rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow();

        return mapToResponse(ride);
    }

    public List<RideResponse> getRidesByPassenger(UUID passengerId) {

        passengerRepository.findById(passengerId)
                .orElseThrow();

        return rideRepository.findByPassengerId(passengerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<RideResponse> getRidesByDriver(UUID driverId) {

        driverRepository.findById(driverId)
                .orElseThrow();

        return rideRepository.findByDriverId(driverId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<RideResponse> getAllRides() {

        return rideRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}