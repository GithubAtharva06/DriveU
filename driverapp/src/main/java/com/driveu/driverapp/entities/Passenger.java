package com.driveu.driverapp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Passenger {
    @Id
    private UUID id;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(?:\\+91|91)?[6-9]\\d{9}$",
            message = "Invalid Indian phone number. Must be a valid 10-digit number optionally starting with +91 or 91."
    )
    @Column(nullable = false, unique = true)
    private String phoneNo;

    @NotBlank
    private String userName;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    private LocalDateTime createdAt;

    private boolean active;
}
