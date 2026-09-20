package com.driveu.driverapp.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvailabilityUpdateRequest {

    @NotNull(message = "Availability cannot be null")
    private Boolean available;
}