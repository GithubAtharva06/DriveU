package com.driveu.driverapp.dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(?:\\+91|91)?[6-9]\\d{9}$",
            message = "Invalid Indian phone number. Must be a valid 10-digit number optionally starting with +91 or 91."
    )
    private String phoneNo;

    @NotBlank(message = "Username is required")
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 100,
            message = "Password must be between 8 and 100 characters"
    )
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*]).{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@#$%^&+=!*)"
    )
    private String password;
}