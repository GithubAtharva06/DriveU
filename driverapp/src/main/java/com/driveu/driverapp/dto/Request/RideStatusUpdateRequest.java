package com.driveu.driverapp.dto.Request;

import com.driveu.driverapp.entities.RideStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RideStatusUpdateRequest {

    @NotNull(message = "Ride status is required")
    private RideStatus rideStatus;
}