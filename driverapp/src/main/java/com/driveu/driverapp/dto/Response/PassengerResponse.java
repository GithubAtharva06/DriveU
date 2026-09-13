package com.driveu.driverapp.dto.Response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class PassengerResponse {

    private UUID id;
    private String phoneNo;
    private String userName;
    private String email;
    private LocalDateTime createdAt;
}