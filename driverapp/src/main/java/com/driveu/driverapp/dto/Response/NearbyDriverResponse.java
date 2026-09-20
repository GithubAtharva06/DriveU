package com.driveu.driverapp.dto.Response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NearbyDriverResponse {

    private UUID driverId;

    private Double latitude;

    private Double longitude;

    private Double distanceInKm;
}