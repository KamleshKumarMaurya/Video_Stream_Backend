package com.streaming.demo.service;

import com.razorpay.Order;
import com.streaming.demo.dto.PaymentResponseDto;
import com.streaming.demo.entity.*;
import com.streaming.demo.payment.RazorpayService;
import com.streaming.demo.repository.*;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

	@Autowired
	private PlanRepository planRepository;

	@Autowired
	private SubscriptionRepository subscriptionRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private RazorpayService razorpayService;

	public List<Plan> getAllPlans() {
		return planRepository.findAll();
	}

	public String initiatePaymentOneTime(Long planId, User user, boolean useTrial) throws Exception {

		Plan plan = planRepository.findById(planId).orElseThrow(() -> new RuntimeException("Plan not found"));
		Optional<Subscription> existingSub = subscriptionRepository
				.findTopByUserAndPlanAndPaymentStatusOrderByStartDateDesc(user, plan, "INITIATED");

		if (existingSub.isPresent()) {
			return existingSub.get().getRazorpaySubscriptionId(); // 🔥 reuse
		}
		// Ensure Razorpay Plan ID exists
		double amount = 0.0;
		if (useTrial) {
			amount = plan.getTrialPrice();
		} else {
			amount = plan.getPrice();
		}
		String rzPlanId = razorpayService.createPlan(plan.getName(), amount, user);

		Subscription subscription = Subscription.builder().user(user).plan(plan).startDate(LocalDateTime.now())
				.expiryDate(null).paymentStatus("INITIATED").razorpaySubscriptionId(rzPlanId).active(false).build();

		subscriptionRepository.save(subscription);

		return rzPlanId;
	}

	public ResponseEntity<com.streaming.demo.common.ApiResponse> verifyPayment(String orderId, String paymentId,
			String signature, boolean trial) {
		boolean isValid = razorpayService.verifySignature(orderId, paymentId, signature);

		if (!isValid) {
			return new ResponseEntity<>(new com.streaming.demo.common.ApiResponse(Boolean.TRUE, "Invalid payment!",
					null, "200", HttpStatus.OK), HttpStatus.BAD_REQUEST);
		}
		// ✅ Update DB
		Optional<Subscription> optionalSub = subscriptionRepository.findByRazorpaySubscriptionId(orderId);
		if (optionalSub.isEmpty()) {
			return new ResponseEntity<>(new com.streaming.demo.common.ApiResponse(Boolean.TRUE,
					"Subscription not found!", null, "200", HttpStatus.OK), HttpStatus.NOT_FOUND);
		}
		Subscription sub = optionalSub.get();
		if (!"SUCCESS".equals(sub.getPaymentStatus())) {
			sub.setPaymentStatus("SUCCESS");
			sub.setActive(true);
		}
		LocalDateTime now = LocalDateTime.now();
		Plan plan = sub.getPlan();
		User user = sub.getUser();
		LocalDateTime expiryDate = null;
		if (trial) {
			expiryDate = now.plusDays(plan.getTrialDurationDays());
			sub.setExpiryDate(expiryDate);
			user.setTrialPlanUsed(true);
		} else {
			expiryDate = now.plusDays(plan.getDurationDays());
			sub.setExpiryDate(expiryDate);
			sub.setNormalPlanActive(true);
		}
		subscriptionRepository.save(sub);
		
		user.setSubscriptionActive(true);
		user.setSubscriptionExpiry(expiryDate);
		userRepository.save(user);

		// ✅ Prepare response
		PaymentResponseDto responseDto = PaymentResponseDto.builder().subscriptionActive(user.isSubscriptionActive())
				.subscriptionExpiry(user.getSubscriptionExpiry()).isTrialPlanUsed(user.isTrialPlanUsed()).build();
		return new ResponseEntity<>(new com.streaming.demo.common.ApiResponse(Boolean.TRUE,
				"verified & subscription activated!", responseDto, "200", HttpStatus.OK), HttpStatus.OK);
	}

	public String initiatePayment(Long planId, User user, boolean useTrial) throws Exception {
		Plan plan = planRepository.findById(planId).orElseThrow(() -> new RuntimeException("Plan not found"));

		// Ensure Razorpay Plan ID exists
//		if (plan.getRazorpayPlanId() == null || plan.getRazorpayPlanId().isEmpty()) {
//			String rzPlanId = razorpayService.createPlan(plan.getName(), plan.getPrice());
//			plan.setRazorpayPlanId(rzPlanId);
//			planRepository.save(plan);
//		}

		double setupFee = (useTrial && plan.isTrialAvailable()) ? plan.getTrialPrice() : 0;
		int trialDays = (useTrial && plan.isTrialAvailable()) ? plan.getTrialDurationDays() : 0;

		String subscriptionId = razorpayService.createSubscription(plan.getRazorpayPlanId(), trialDays, setupFee,
				String.valueOf(user.getId()));

		// Save Subscription as INITIATED
		Subscription subscription = Subscription.builder().user(user).plan(plan).startDate(LocalDateTime.now())
				.paymentStatus("INITIATED").razorpaySubscriptionId(subscriptionId).active(false).build();
		subscriptionRepository.save(subscription);

		return subscriptionId;
	}

	@Transactional
	public void captureAndActivateSubscription(String subscriptionId, String paymentId, String signature, Long planId,
			User user, boolean useTrial) throws Exception {

		boolean isValid = razorpayService.verifySignature(subscriptionId, paymentId, signature);

		if (isValid) {
			Plan plan = planRepository.findById(planId).orElseThrow(() -> new RuntimeException("Plan not found"));

			boolean isTrial = useTrial && plan.isTrialAvailable();
			double amount = isTrial ? plan.getTrialPrice() : plan.getPrice();
			int duration = isTrial ? plan.getTrialDurationDays() : plan.getDurationDays();

			// Save Payment
			Payment payment = Payment.builder().user(user).amount(amount).paymentMethod("RAZORPAY")
					.transactionId(paymentId).status("SUCCESS").build();
			paymentRepository.save(payment);

			// Update or Create Subscription
			Subscription subscription = subscriptionRepository.findByRazorpaySubscriptionId(subscriptionId).orElse(
					Subscription.builder().user(user).plan(plan).razorpaySubscriptionId(subscriptionId).build());

			LocalDateTime now = LocalDateTime.now();
			subscription.setStartDate(now);
			subscription.setExpiryDate(now.plusDays(duration));
			subscription.setPaymentStatus(isTrial ? "TRIAL_PAID" : "PAID");
			subscription.setActive(true);

			subscriptionRepository.save(subscription);

			// Update user status
			user.setSubscriptionActive(true);
			user.setSubscriptionExpiry(subscription.getExpiryDate());
			userRepository.save(user);
		} else {
			throw new RuntimeException("Invalid Razorpay Signature");
		}
	}
}
