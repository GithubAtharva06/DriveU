package com.driveu.driverapp.dto.Response;

import com.driveu.driverapp.entities.StatusCheck;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class DriverResponse {

    private UUID id;
    private String phoneNo;
    private String userName;
    private String email;
    private String licenseNumber;
    private StatusCheck status;
    private LocalDateTime createdAt;
}