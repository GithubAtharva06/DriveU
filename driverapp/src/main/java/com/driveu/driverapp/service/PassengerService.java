package com.driveu.driverapp.service;

import com.driveu.driverapp.dto.Request.PassengerRequest;
import com.driveu.driverapp.dto.Response.PassengerResponse;
import com.driveu.driverapp.entities.Passenger;
import com.driveu.driverapp.repository.PassengerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PassengerService {
    private final PassengerRepository passengerRepository;

    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    private PassengerResponse mapToResponse(Passenger passenger) {
        PassengerResponse response = new PassengerResponse();

        response.setId(passenger.getId());
        response.setPhoneNo(passenger.getPhoneNo());
        response.setUserName(passenger.getUserName());
        response.setEmail(passenger.getEmail());
        response.setCreatedAt(passenger.getCreatedAt());

        return response;
    }

    public PassengerResponse registerPassenger(PassengerRequest request) {

        if (passengerRepository.existsByPhoneNo(request.getPhoneNo())) {
            throw new RuntimeException("Phone number is already registered");
        }

        if (passengerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        Passenger passenger = new Passenger();

        passenger.setPhoneNo(request.getPhoneNo());
        passenger.setUserName(request.getUserName());
        passenger.setEmail(request.getEmail());
        passenger.setPassword(request.getPassword());
        passenger.setId(UUID.randomUUID());
        passenger.setCreatedAt(LocalDateTime.now());

        passengerRepository.save(passenger);

        return mapToResponse(passenger);
    }

    public PassengerResponse getPassengerById(UUID id){
        Passenger passenger = passengerRepository.findById(id).orElseThrow();

        return mapToResponse(passenger);
    }

    public List<PassengerResponse> getAllPassengers() {
        List<Passenger> passengers = passengerRepository.findAll();

        return passengers.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PassengerResponse updatePassenger(UUID id, PassengerRequest request) {

        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow();

        if (passengerRepository.existsByPhoneNoAndIdNot(request.getPhoneNo(), id)) {
            throw new RuntimeException("Phone number is already registered");
        }

        if (passengerRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new RuntimeException("Email is already registered");
        }

        passenger.setPhoneNo(request.getPhoneNo());
        passenger.setUserName(request.getUserName());
        passenger.setEmail(request.getEmail());
        passenger.setPassword(request.getPassword());

        passengerRepository.save(passenger);

        return mapToResponse(passenger);
    }

    public void deactivatePassenger(UUID id) {
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow();

        passenger.setActive(false);

        passengerRepository.save(passenger);
    }

    public void reactivatePassenger(UUID id) {
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow();

        passenger.setActive(true);

        passengerRepository.save(passenger);
    }

    public void deletePassenger(UUID id) {
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow();

        passengerRepository.delete(passenger);
    }


}
