package com.driveu.driverapp.dto.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RideRequest {

    private UUID passengerId;
    private String pickupLocation;
    private String dropLocation;
}