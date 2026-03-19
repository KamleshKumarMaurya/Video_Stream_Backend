package com.streaming.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDetailDto {
    private Long id;
    private String name;
    private String email;
    private String mobileNo;
    private boolean subscriptionActive;
    private boolean active;
    private boolean isTrialPlanUsed;
    
    // Subscription Details
    private Long activePlanId;
    private String activePlanName;
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionExpiryDate;
    private boolean isNormalPlanActive;
    // Payment History
    private List<PaymentHistoryDto> paymentHistory;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentHistoryDto {
        private Long id;
        private double amount;
        private String paymentMethod;
        private String transactionId;
        private String status;
    }
}
