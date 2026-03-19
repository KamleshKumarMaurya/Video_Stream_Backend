package com.streaming.demo.payment;

import com.razorpay.Order;
import com.razorpay.Plan;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Subscription;
import com.razorpay.Utils;
import com.streaming.demo.entity.User;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class RazorpayService {

    @Value("${razorpay.keyId}")
    private String keyId;

    @Value("${razorpay.keySecret}")
    private String keySecret;

    private RazorpayClient client;

    @PostConstruct
    public void init() throws RazorpayException {
        this.client = new RazorpayClient(keyId, keySecret);
    }

    public String createPlan(String name, double amount, User user) throws RazorpayException {    	
    	JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", (int) Math.round(amount * 100));
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "order_" + System.currentTimeMillis());

        // ✅ Add custom data
        JSONObject notes = new JSONObject();
        notes.put("userId", user.getId());
//        notes.put("type", "subscription"); // optional

        orderRequest.put("notes", notes);

        Order order = client.orders.create(orderRequest);

        return order.get("id");
    }
    
    public String createSubscription(String planId, int trialDays, String userId) throws RazorpayException {

        JSONObject subscriptionRequest = new JSONObject();

        subscriptionRequest.put("plan_id", planId);
        subscriptionRequest.put("total_count", 12); // or large value
        subscriptionRequest.put("quantity", 1);
        subscriptionRequest.put("customer_notify", 1);

        // ✅ Attach user info
        JSONObject notes = new JSONObject();
        notes.put("user_id", userId);
        subscriptionRequest.put("notes", notes);

        // ✅ Correct trial handling
        if (trialDays > 0) {
            long trialEnd = (System.currentTimeMillis() / 1000) + (trialDays * 24 * 60 * 60);
            subscriptionRequest.put("trial_end", trialEnd);
        }

        try {
            Subscription subscription = client.subscriptions.create(subscriptionRequest);
            return subscription.get("id");

        } catch (Exception e) {

            System.out.println("MAIN ERROR: " + e.getMessage());

            if (e.getCause() != null) {
                System.out.println("ACTUAL ERROR: " + e.getCause().getMessage());
            }

            e.printStackTrace();
            throw e;
        }
    }

    public String createSubscription(String planId, int trialDays, double setupFee, String userId) throws RazorpayException {
        JSONObject subscriptionRequest = new JSONObject();
        subscriptionRequest.put("plan_id", planId);
        subscriptionRequest.put("total_count", 12);
        subscriptionRequest.put("quantity", 1);
        subscriptionRequest.put("customer_notify", 1);
        
        JSONObject notes = new JSONObject();
        notes.put("user_id", userId);
        subscriptionRequest.put("notes", notes);
        
        if (trialDays > 0) {
            long startAt = (System.currentTimeMillis() / 1000) + (trialDays * 24 * 60 * 60);
            subscriptionRequest.put("start_at", startAt);
        }

        if (setupFee > 0) {
            JSONObject addon = new JSONObject();
            JSONObject item = new JSONObject();
            item.put("name", "Trial Fee");
            item.put("amount", (int)(setupFee * 100));
            item.put("currency", "INR");
            addon.put("item", item);
            subscriptionRequest.put("addons", new org.json.JSONArray().put(addon));
        }

        try {
            Subscription subscription = client.subscriptions.create(subscriptionRequest);
            return subscription.get("id");

        } catch (Exception e) {

            System.out.println("MAIN ERROR: " + e.getMessage());

            if (e.getCause() != null) {
                System.out.println("ACTUAL ERROR: " + e.getCause().getMessage());
            }

            e.printStackTrace();
            throw e;
        }
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
        	 String payload = orderId + "|" + paymentId;
            return Utils.verifySignature(payload, signature, keySecret);
        } catch (Exception e) {
            return false;
        }
    }
}
