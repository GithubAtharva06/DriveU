package com.driveu.driverapp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class DriverLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID locationId;

    @OneToOne
    @JoinColumn(name = "driver_id", nullable = false, unique = true)
    private Driver driver;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Boolean available = false;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}