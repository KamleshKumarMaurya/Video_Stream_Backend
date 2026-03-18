package com.streaming.demo.service;

import com.streaming.demo.dto.JwtResponse;
import com.streaming.demo.entity.Role;
import com.streaming.demo.entity.User;
import com.streaming.demo.repository.UserRepository;
import com.streaming.demo.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private OtpService otpService;

    public void registerUser(String name, String email, String mobileNo, String password, Role role) {
        if (email != null && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Error: Email is already in use!");
        }
        if (mobileNo != null && userRepository.existsByMobileNo(mobileNo)) {
            throw new RuntimeException("Error: Mobile number is already in use!");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .mobileNo(mobileNo)
                .password(encoder.encode(password))
                .role(role)
                .subscriptionActive(false)
                .build();

        userRepository.save(user);
    }

    public JwtResponse login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        User user = userRepository.findByEmail(email).get();
        return new JwtResponse(jwt, email, user);
    }

    public void sendOtp(String mobileNo) {
        userRepository.findByMobileNo(mobileNo).ifPresent(user -> {
            if (!user.isActive()) {
                throw new RuntimeException("Account is inactive. Please contact support.");
            }
        });
        otpService.generateOtp(mobileNo);
    }

    public com.streaming.demo.dto.JwtResponse verifyOtpAndLogin(String mobileNo, String otp) throws Exception {
        if (!otpService.validateOtp(mobileNo, otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user;
        if (!userRepository.existsByMobileNo(mobileNo)) {
            // Register as CUSTOMER if not exists
            user = User.builder()
                    .mobileNo(mobileNo)
                    .password(encoder.encode(mobileNo)) // Use mobileNo as default password
                    .role(Role.CUSTOMER)
                    .subscriptionActive(false)
                    .build();
            userRepository.save(user);
        } else {
            user = userRepository.findByMobileNo(mobileNo).get();
            if (user.getRole() != Role.CUSTOMER) {
                throw new RuntimeException("Unauthorized role for mobile login");
            }
        }

        // Authenticate using the mobile number as both username and password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(mobileNo, mobileNo));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        return new com.streaming.demo.dto.JwtResponse(jwt, user.getEmail(), user);
    }
}
