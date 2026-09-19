package com.driveu.driverapp.dto.Request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RideRequest {

    private UUID passengerId;

    private String pickupLocation;
    private Double pickupLatitude;
    private Double pickupLongitude;

    private String dropLocation;
    private Double dropLatitude;
    private Double dropLongitude;
}