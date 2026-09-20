package com.driveu.driverapp.dto.Response;

import com.driveu.driverapp.entities.OfferStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class RideOfferResponse {

    private UUID offerId;
    private UUID rideId;
    private UUID driverId;
    private OfferStatus offerStatus;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}