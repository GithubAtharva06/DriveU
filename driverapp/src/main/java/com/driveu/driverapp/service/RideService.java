package com.driveu.driverapp.service;

import com.driveu.driverapp.dto.Response.RideResponse;
import com.driveu.driverapp.entities.Ride;
import com.driveu.driverapp.repository.DriverRepository;
import com.driveu.driverapp.repository.PassengerRepository;
import com.driveu.driverapp.repository.RideRepository;
import org.springframework.stereotype.Service;
import com.driveu.driverapp.dto.Request.RideRequest;
import com.driveu.driverapp.entities.Passenger;
import com.driveu.driverapp.entities.RideStatus;
import com.driveu.driverapp.entities.Driver;

import jakarta.transaction.Transactional;
import java.util.Arrays;
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
        response.setPickupLatitude(ride.getPickupLatitude());
        response.setPickupLongitude(ride.getPickupLongitude());

        response.setDropLocation(ride.getDropLocation());
        response.setDropLatitude(ride.getDropLatitude());
        response.setDropLongitude(ride.getDropLongitude());

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

    public RideResponse createRide(RideRequest request) {

        Passenger passenger = passengerRepository.findById(request.getPassengerId())
                .orElseThrow(() -> new RuntimeException("Passenger not found"));

        List<RideStatus> activeStatuses = Arrays.asList(
                RideStatus.REQUESTED,
                RideStatus.ACCEPTED,
                RideStatus.ARRIVING,
                RideStatus.ARRIVED,
                RideStatus.IN_PROGRESS
        );

        boolean hasActiveRide =
                rideRepository.existsByPassengerIdAndRideStatusIn(
                        passenger.getId(),
                        activeStatuses
                );

        if (hasActiveRide) {
            throw new RuntimeException(
                    "Passenger already has an active ride"
            );
        }

        Ride ride = new Ride();

        ride.setRideId(UUID.randomUUID());
        ride.setPassenger(passenger);

        ride.setPickupLocation(request.getPickupLocation());
        ride.setPickupLatitude(request.getPickupLatitude());
        ride.setPickupLongitude(request.getPickupLongitude());

        ride.setDropLocation(request.getDropLocation());
        ride.setDropLatitude(request.getDropLatitude());
        ride.setDropLongitude(request.getDropLongitude());

        ride.setRideStatus(RideStatus.REQUESTED);

        Ride savedRide = rideRepository.save(ride);

        return mapToResponse(savedRide);
    }

    @Transactional
    public RideResponse acceptRide(UUID rideId, UUID driverId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Ride must still be requested
        if (ride.getRideStatus() != RideStatus.REQUESTED) {
            throw new RuntimeException("Ride is no longer available");
        }

        // Driver cannot accept another active ride
        List<RideStatus> activeStatuses = Arrays.asList(
                RideStatus.ACCEPTED,
                RideStatus.ARRIVING,
                RideStatus.ARRIVED,
                RideStatus.IN_PROGRESS
        );

        boolean driverHasActiveRide =
                rideRepository.existsByDriverIdAndRideStatusIn(
                        driverId,
                        activeStatuses
                );

        if (driverHasActiveRide) {
            throw new RuntimeException(
                    "Driver already has an active ride"
            );
        }

        // Assign driver and update status
        ride.setDriver(driver);
        ride.setRideStatus(RideStatus.ACCEPTED);

        Ride savedRide = rideRepository.save(ride);

        return mapToResponse(savedRide);
    }
}