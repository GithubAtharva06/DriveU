package com.driveu.driverapp.repository;

import com.driveu.driverapp.entities.OfferStatus;
import com.driveu.driverapp.entities.RideOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RideOfferRepository
        extends JpaRepository<RideOffer, UUID> {

    List<RideOffer> findByRide_RideId(UUID rideId);

    List<RideOffer> findByDriver_Id(UUID driverId);

    boolean existsByRide_RideIdAndDriver_Id(
            UUID rideId,
            UUID driverId
    );

    List<RideOffer> findByRide_RideIdAndOfferStatus(
            UUID rideId,
            OfferStatus offerStatus
    );
}