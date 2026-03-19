package com.streaming.demo.controller;

import com.streaming.demo.dto.MessageResponse;
import com.streaming.demo.entity.User;
import com.streaming.demo.security.UserDetailsImpl;
import com.streaming.demo.service.SubscriptionService;
import com.streaming.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

	@Autowired
	private SubscriptionService subscriptionService;

	@Autowired
	private UserRepository userRepository;

	@PostMapping("/initiate")
	public ResponseEntity<?> initiatePayment(@RequestParam Long planId,
			@RequestParam(defaultValue = "false") boolean trial, @AuthenticationPrincipal UserDetailsImpl userDetails) {
		try {
			if (userDetails != null) {
				User user = userRepository.findById(userDetails.getId()).orElseThrow();
				String subscriptionId = subscriptionService.initiatePaymentOneTime(planId, user, trial);
				return ResponseEntity.ok(new MessageResponse(subscriptionId));
			}
			return ResponseEntity.ok(new MessageResponse(null));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(new MessageResponse("Error initiating payment: " + e.getMessage()));
		}
	}

	@PostMapping("/verify")
	public ResponseEntity<com.streaming.demo.common.ApiResponse> verifyPayment(@RequestParam String orderId,
			@RequestParam String paymentId, @RequestParam String signature,@RequestParam(defaultValue = "false") boolean trial) {

		return subscriptionService.verifyPayment(orderId, paymentId, signature,trial);

	}

	@PostMapping("/capture")
	public ResponseEntity<?> capturePayment(@RequestParam(name = "subscriptionId") String subscriptionId,
			@RequestParam(name = "paymentId") String paymentId, @RequestParam(name = "signature") String signature,
			@RequestParam(name = "planId") Long planId,
			@RequestParam(name = "trial", defaultValue = "false") boolean trial,
			@AuthenticationPrincipal UserDetailsImpl userDetails) {
		try {
			User user = userRepository.findById(userDetails.getId()).orElseThrow();
			subscriptionService.captureAndActivateSubscription(subscriptionId, paymentId, signature, planId, user,
					trial);
			return ResponseEntity.ok(new MessageResponse("Subscription activated successfully!"));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(new MessageResponse("Error capturing payment: " + e.getMessage()));
		}
	}
}
