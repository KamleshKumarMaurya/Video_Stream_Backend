package com.streaming.demo.controller;

import com.streaming.demo.dto.MessageResponse;
import com.streaming.demo.entity.User;
import com.streaming.demo.security.UserDetailsImpl;
import com.streaming.demo.service.SubscriptionService;
import com.streaming.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(
            @RequestParam(name = "planId") Long planId, 
            @RequestParam(name = "trial", defaultValue = "false") boolean trial,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User user = userRepository.findById(userDetails.getId()).orElseThrow();
            String orderId = subscriptionService.initiatePayment(planId, user, trial);
            return ResponseEntity.ok(new MessageResponse(orderId));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Error initiating payment"));
        }
    }

    @PostMapping("/capture")
    public ResponseEntity<?> capturePayment(
            @RequestParam(name = "orderId") String orderId, 
            @RequestParam(name = "planId") Long planId, 
            @RequestParam(name = "trial", defaultValue = "false") boolean trial,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User user = userRepository.findById(userDetails.getId()).orElseThrow();
            subscriptionService.captureAndActivateSubscription(orderId, planId, user, trial);
            return ResponseEntity.ok(new MessageResponse("Subscription activated successfully!"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Error capturing payment"));
        }
    }
}
