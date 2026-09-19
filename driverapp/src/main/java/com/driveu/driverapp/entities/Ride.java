package com.driveu.driverapp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Ride {
    @Id
    private UUID rideId;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Enumerated(EnumType.STRING)
    private RideStatus rideStatus;

    private String pickupLocation;
    private String dropLocation;

    @Column(nullable = false)
    private Double pickupLatitude;

    @Column(nullable = false)
    private Double pickupLongitude;

    private Double dropLatitude;

    private Double dropLongitude;

    private BigDecimal fare;

    private LocalDateTime pickupAt;
    private LocalDateTime dropOffAt;
}
