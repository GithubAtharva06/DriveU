package com.driveu.driverapp.dto.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverRegistrationRequest {

    private String phoneNo;
    private String userName;
    private String email;
    private String password;
    private String licenseNumber;
}