package com.streaming.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double price;
    private int durationDays;
    
    private double trialPrice;
    private int trialDurationDays;
    private boolean trialAvailable;
    private String razorpayPlanId;
}
