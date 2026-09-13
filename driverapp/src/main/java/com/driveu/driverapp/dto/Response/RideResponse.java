package com.driveu.driverapp.dto.Response;

import com.driveu.driverapp.entities.RideStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class RideResponse {

    private UUID rideId;
    private UUID passengerId;
    private UUID driverId;
    private RideStatus rideStatus;
    private String pickupLocation;
    private String dropLocation;
    private BigDecimal fare;
    private LocalDateTime pickupAt;
    private LocalDateTime dropOffAt;
}