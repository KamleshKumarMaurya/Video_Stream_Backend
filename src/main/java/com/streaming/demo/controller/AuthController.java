package com.streaming.demo.controller;

import com.streaming.demo.dto.*;
import com.streaming.demo.entity.Role;
import com.streaming.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            Role role = Role.valueOf(registerRequest.getRole().toUpperCase());
            authService.registerUser(registerRequest.getName(), registerRequest.getEmail(),
                    registerRequest.getMobileNo(), registerRequest.getPassword(), role);
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new MessageResponse("Invalid credentials"));
        }
    }

    @PostMapping("/mobile/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody OtpRequest otpRequest) {
        try {
            authService.sendOtp(otpRequest.getMobileNo());
            return ResponseEntity.ok(new MessageResponse("OTP sent successfully to " + otpRequest.getMobileNo()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/mobile/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
        try {
            JwtResponse jwtResponse = authService.verifyOtpAndLogin(verifyOtpRequest.getMobileNo(),
                    verifyOtpRequest.getOtp());
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new MessageResponse(e.getMessage()));
        }
    }
}
