package com.streaming.demo.service;

import com.streaming.demo.entity.*;
import com.streaming.demo.payment.PayPalService;
import com.streaming.demo.repository.*;
import com.paypal.orders.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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
    private PayPalService payPalService;

    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    public String initiatePayment(Long planId, User user, boolean useTrial) throws IOException {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        
        double amount = (useTrial && plan.isTrialAvailable()) ? plan.getTrialPrice() : plan.getPrice();
        return payPalService.createOrder(amount);
    }

    @Transactional
    public void captureAndActivateSubscription(String orderId, Long planId, User user, boolean useTrial) throws IOException {
        Order order = payPalService.captureOrder(orderId);
        
        if ("COMPLETED".equals(order.status())) {
            Plan plan = planRepository.findById(planId)
                    .orElseThrow(() -> new RuntimeException("Plan not found"));

            boolean isTrial = useTrial && plan.isTrialAvailable();
            double amount = isTrial ? plan.getTrialPrice() : plan.getPrice();
            int duration = isTrial ? plan.getTrialDurationDays() : plan.getDurationDays();

            // Save Payment
            Payment payment = Payment.builder()
                    .user(user)
                    .amount(amount)
                    .paymentMethod("PAYPAL")
                    .transactionId(order.id())
                    .status("SUCCESS")
                    .build();
            paymentRepository.save(payment);

            // Activate Subscription
            LocalDateTime now = LocalDateTime.now();
            Subscription subscription = Subscription.builder()
                    .user(user)
                    .plan(plan)
                    .startDate(now)
                    .expiryDate(now.plusDays(duration))
                    .paymentStatus(isTrial ? "TRIAL" : "PAID")
                    .build();
            subscriptionRepository.save(subscription);

            // Update user status
            user.setSubscriptionActive(true);
            user.setSubscriptionExpiry(subscription.getExpiryDate());
            userRepository.save(user);
        }
    }
}
