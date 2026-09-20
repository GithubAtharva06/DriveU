package com.driveu.driverapp.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RideOfferRequest {

    @NotNull(message = "Ride ID is required")
    private UUID rideId;
}