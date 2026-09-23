package com.driveu.driverapp.service;

import com.driveu.driverapp.dto.Request.RideRequest;
import com.driveu.driverapp.dto.Response.RideResponse;
import com.driveu.driverapp.entities.Driver;
import com.driveu.driverapp.entities.Passenger;
import com.driveu.driverapp.entities.Ride;
import com.driveu.driverapp.entities.RideStatus;
import com.driveu.driverapp.exception.BusinessException;
import com.driveu.driverapp.exception.ResourceNotFoundException;
import com.driveu.driverapp.repository.DriverRepository;
import com.driveu.driverapp.repository.PassengerRepository;
import com.driveu.driverapp.repository.RideRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    private List<RideStatus> getPassengerActiveStatuses() {

        return Arrays.asList(
                RideStatus.REQUESTED,
                RideStatus.ACCEPTED,
                RideStatus.ARRIVING,
                RideStatus.ARRIVED,
                RideStatus.IN_PROGRESS
        );
    }

    private List<RideStatus> getDriverActiveStatuses() {

        return Arrays.asList(
                RideStatus.ACCEPTED,
                RideStatus.ARRIVING,
                RideStatus.ARRIVED,
                RideStatus.IN_PROGRESS
        );
    }

    public RideResponse getRideById(UUID rideId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ride not found with ID: " + rideId
                        ));

        return mapToResponse(ride);
    }

    public List<RideResponse> getRidesByPassenger(UUID passengerId) {

        passengerRepository.findById(passengerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Passenger not found with ID: " + passengerId
                        ));

        return rideRepository.findByPassengerId(passengerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<RideResponse> getRidesByDriver(UUID driverId) {

        driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver not found with ID: " + driverId
                        ));

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

    @Transactional
    public RideResponse createRide(RideRequest request) {

        Passenger passenger =
                passengerRepository.findById(request.getPassengerId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Passenger not found with ID: "
                                                + request.getPassengerId()
                                ));

        if (!passenger.isActive()) {
            throw new BusinessException(
                    "Passenger account is inactive"
            );
        }

        boolean hasActiveRide =
                rideRepository.existsByPassengerIdAndRideStatusIn(
                        passenger.getId(),
                        getPassengerActiveStatuses()
                );

        if (hasActiveRide) {
            throw new BusinessException(
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
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ride not found with ID: " + rideId
                        ));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Driver not found with ID: " + driverId
                        ));

        if (ride.getRideStatus() != RideStatus.REQUESTED) {
            throw new BusinessException(
                    "Ride is no longer available"
            );
        }

        if (driver.getStatus() == null ||
                !driver.getStatus().name().equals("ONLINE")) {

            throw new BusinessException(
                    "Driver must be online to accept a ride"
            );
        }

        boolean driverHasActiveRide =
                rideRepository.existsByDriverIdAndRideStatusIn(
                        driverId,
                        getDriverActiveStatuses()
                );

        if (driverHasActiveRide) {
            throw new BusinessException(
                    "Driver already has an active ride"
            );
        }

        ride.setDriver(driver);
        ride.setRideStatus(RideStatus.ACCEPTED);

        Ride savedRide = rideRepository.save(ride);

        return mapToResponse(savedRide);
    }

    @Transactional
    public RideResponse updateRideStatus(
            UUID rideId,
            UUID driverId,
            RideStatus newStatus
    ) {

        if (newStatus == null) {
            throw new BusinessException(
                    "Ride status cannot be null"
            );
        }

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ride not found with ID: " + rideId
                        ));

        if (ride.getDriver() == null) {
            throw new BusinessException(
                    "No driver assigned to this ride"
            );
        }

        if (!ride.getDriver().getId().equals(driverId)) {
            throw new BusinessException(
                    "You are not assigned to this ride"
            );
        }

        RideStatus currentStatus = ride.getRideStatus();

        boolean validTransition = switch (currentStatus) {

            case ACCEPTED ->
                    newStatus == RideStatus.ARRIVING;

            case ARRIVING ->
                    newStatus == RideStatus.ARRIVED;

            case ARRIVED ->
                    newStatus == RideStatus.IN_PROGRESS;

            case IN_PROGRESS ->
                    newStatus == RideStatus.COMPLETED;

            default -> false;
        };

        if (!validTransition) {
            throw new BusinessException(
                    "Invalid ride status transition from "
                            + currentStatus + " to " + newStatus
            );
        }

        if (newStatus == RideStatus.IN_PROGRESS) {

            if (ride.getPickupAt() != null) {
                throw new BusinessException(
                        "Pickup time has already been recorded"
                );
            }

            ride.setPickupAt(LocalDateTime.now());
        }

        if (newStatus == RideStatus.COMPLETED) {

            if (ride.getPickupAt() == null) {
                throw new BusinessException(
                        "Pickup time must be recorded before completion"
                );
            }

            ride.setDropOffAt(LocalDateTime.now());
        }

        ride.setRideStatus(newStatus);

        Ride savedRide = rideRepository.save(ride);

        return mapToResponse(savedRide);
    }

    @Transactional
    public RideResponse cancelRide(UUID rideId, UUID passengerId) {

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ride not found with ID: " + rideId
                        ));

        if (!ride.getPassenger().getId().equals(passengerId)) {
            throw new BusinessException(
                    "You are not the passenger of this ride"
            );
        }

        RideStatus currentStatus = ride.getRideStatus();

        boolean cancellable = switch (currentStatus) {

            case REQUESTED,
                 ACCEPTED,
                 ARRIVING,
                 ARRIVED -> true;

            default -> false;
        };

        if (!cancellable) {
            throw new BusinessException(
                    "Ride cannot be cancelled in status "
                            + currentStatus
            );
        }

        ride.setRideStatus(RideStatus.CANCELLED);

        Ride savedRide = rideRepository.save(ride);

        return mapToResponse(savedRide);
    }
}