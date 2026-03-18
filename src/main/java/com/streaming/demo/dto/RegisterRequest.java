package com.streaming.demo.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String mobileNo;
    private String password;
    private String role; // ADMIN or CUSTOMER
}
