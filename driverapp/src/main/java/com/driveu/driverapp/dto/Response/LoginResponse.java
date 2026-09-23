package com.driveu.driverapp.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private UUID id;
    private String userName;
    private String email;
    private String role;
    private String token;
}