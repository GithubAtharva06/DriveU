package com.driveu.driverapp.dto.Response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class DriverLocationResponse {

    private UUID locationId;

    private UUID driverId;

    private Double latitude;

    private Double longitude;

    private LocalDateTime updatedAt;
}