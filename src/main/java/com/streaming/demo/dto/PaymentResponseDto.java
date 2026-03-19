package com.streaming.demo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponseDto {
    private boolean subscriptionActive;
    private boolean isTrialPlanUsed;
    private LocalDateTime subscriptionExpiry;
}