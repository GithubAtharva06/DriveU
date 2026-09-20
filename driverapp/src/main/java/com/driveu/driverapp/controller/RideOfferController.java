
package com.driveu.driverapp.controller;

import com.driveu.driverapp.dto.Response.RideOfferResponse;
import com.driveu.driverapp.service.RideOfferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ride-offers")
public class RideOfferController {

    private final RideOfferService rideOfferService;

    public RideOfferController(
            RideOfferService rideOfferService
    ) {
        this.rideOfferService = rideOfferService;
    }

    // GENERATE OFFERS FOR NEARBY DRIVERS

    @PostMapping("/generate/{rideId}")
    public ResponseEntity<List<RideOfferResponse>> generateOffers(
            @PathVariable UUID rideId
    ) {

        List<RideOfferResponse> offers =
                rideOfferService.generateOffersForRide(rideId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(offers);
    }

    // GET OFFER BY ID

    @GetMapping("/{offerId}")
    public ResponseEntity<RideOfferResponse> getOfferById(
            @PathVariable UUID offerId
    ) {

        RideOfferResponse response =
                rideOfferService.getOfferById(offerId);

        return ResponseEntity.ok(response);
    }

    // GET ALL OFFERS FOR A RIDE

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<List<RideOfferResponse>> getOffersByRide(
            @PathVariable UUID rideId
    ) {

        List<RideOfferResponse> responses =
                rideOfferService.getOffersByRide(rideId);

        return ResponseEntity.ok(responses);
    }

    // GET ALL OFFERS FOR A DRIVER

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<RideOfferResponse>> getOffersByDriver(
            @PathVariable UUID driverId
    ) {

        List<RideOfferResponse> responses =
                rideOfferService.getOffersByDriver(driverId);

        return ResponseEntity.ok(responses);
    }

    // DECLINE OFFER

    @PatchMapping("/{offerId}/decline/{driverId}")
    public ResponseEntity<RideOfferResponse> declineOffer(
            @PathVariable UUID offerId,
            @PathVariable UUID driverId
    ) {

        RideOfferResponse response =
                rideOfferService.declineOffer(
                        offerId,
                        driverId
                );

        return ResponseEntity.ok(response);
    }

    // ACCEPT OFFER

    @PatchMapping("/{offerId}/accept/{driverId}")
    public ResponseEntity<RideOfferResponse> acceptOffer(
            @PathVariable UUID offerId,
            @PathVariable UUID driverId
    ) {

        RideOfferResponse response =
                rideOfferService.acceptOffer(
                        offerId,
                        driverId
                );

        return ResponseEntity.ok(response);
    }
}