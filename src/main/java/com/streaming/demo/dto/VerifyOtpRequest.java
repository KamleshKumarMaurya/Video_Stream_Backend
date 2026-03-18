package com.streaming.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank
    private String mobileNo;
    
    @NotBlank
    private String otp;
}
