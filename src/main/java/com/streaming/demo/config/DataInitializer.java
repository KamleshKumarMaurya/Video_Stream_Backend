package com.streaming.demo.config;

import com.streaming.demo.entity.Plan;
import com.streaming.demo.entity.Role;
import com.streaming.demo.repository.PlanRepository;
import com.streaming.demo.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(AuthService authService, PlanRepository planRepository) {
        return args -> {
            // Initialize Admin
            try {
                authService.registerUser("Admin", "admin@streaming.com", null, "admin123", Role.ADMIN);
            } catch (Exception ignored) {}

            // Initialize Plans
            if (planRepository.count() == 0) {
                // Trial: 7 days for 1 Rupee, then 3 months for 299
                planRepository.save(Plan.builder()
                        .name("Premium 3 Months")
                        .price(299.0)
                        .durationDays(90)
                        .trialAvailable(true)
                        .trialPrice(1.0)
                        .trialDurationDays(7)
                        .build());

                planRepository.save(Plan.builder()
                        .name("Premium 6 Months")
                        .price(499.0)
                        .durationDays(180)
                        .trialAvailable(true)
                        .trialPrice(1.0)
                        .trialDurationDays(7)
                        .build());

                planRepository.save(Plan.builder()
                        .name("Premium 12 Months")
                        .price(899.0)
                        .durationDays(365)
                        .trialAvailable(true)
                        .trialPrice(1.0)
                        .trialDurationDays(7)
                        .build());
            }
        };
    }
}
