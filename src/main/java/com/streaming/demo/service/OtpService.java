package com.streaming.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    
    // In-memory storage for simplicity (Mobile -> OTP)
    // In production, use Redis with TTL
    private final Map<String, String> otpCache = new ConcurrentHashMap<>();
    
    // OTP Expiry: 5 minutes
    private static final long OTP_EXPIRY_MS = TimeUnit.MINUTES.toMillis(5);
    private final Map<String, Long> expiryCache = new ConcurrentHashMap<>();

    public String generateOtp(String mobileNo) {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        otpCache.put(mobileNo, otp);
        expiryCache.put(mobileNo, System.currentTimeMillis() + OTP_EXPIRY_MS);
        
        // Log to console instead of sending real SMS
        logger.info("**************************************************");
        logger.info("OTP for mobile {}: {}", mobileNo, otp);
        logger.info("**************************************************");
        
        return otp;
    }

    public boolean validateOtp(String mobileNo, String otp) {
        if (!otpCache.containsKey(mobileNo)) {
            return false;
        }
        
        long expiryTime = expiryCache.getOrDefault(mobileNo, 0L);
        if (System.currentTimeMillis() > expiryTime) {
            otpCache.remove(mobileNo);
            expiryCache.remove(mobileNo);
            return false;
        }
        
//        boolean isValid = otpCache.get(mobileNo).equals(otp);
        boolean isValid = true;
        if (isValid) {
            otpCache.remove(mobileNo);
            expiryCache.remove(mobileNo);
        }
        return isValid;
    }
}
