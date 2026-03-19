package com.streaming.demo.service;

import com.streaming.demo.dto.CustomerDetailDto;
import com.streaming.demo.entity.Role;
import com.streaming.demo.entity.Subscription;
import com.streaming.demo.entity.User;
import com.streaming.demo.repository.PaymentRepository;
import com.streaming.demo.repository.SubscriptionRepository;
import com.streaming.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class AdminCustomerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public Page<CustomerDetailDto> getAllCustomers(Pageable pageable) {
        Page<User> customers = userRepository.findByRole(Role.CUSTOMER, pageable);
        return customers.map(this::mapToCustomerDetailDto);
    }

    public CustomerDetailDto getCustomerById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        if (user.getRole() != Role.CUSTOMER) {
            throw new RuntimeException("User is not a customer");
        }
        return mapToCustomerDetailDto(user);
    }

    @Transactional
    public void updateSubscriptionStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        user.setSubscriptionActive(active);
        userRepository.save(user);
    }

    @Transactional
    public void toggleCustomerStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        user.setActive(active);
        userRepository.save(user);
    }

    private CustomerDetailDto mapToCustomerDetailDto(User user) {
        Subscription latestSub = subscriptionRepository.findFirstByUserOrderByExpiryDateDesc(user).orElse(null);
        
        CustomerDetailDto.CustomerDetailDtoBuilder builder = CustomerDetailDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobileNo(user.getMobileNo())
                .subscriptionActive(user.isSubscriptionActive())
                .active(user.isActive())
                .isTrialPlanUsed(user.isTrialPlanUsed());

        if (latestSub != null) {
            builder.activePlanId(latestSub.getPlan().getId())
                    .activePlanName(latestSub.getPlan().getName())
                    .subscriptionStartDate(latestSub.getStartDate())
                    .subscriptionExpiryDate(latestSub.getExpiryDate())
                    .isNormalPlanActive(latestSub.isNormalPlanActive());
        }

        builder.paymentHistory(paymentRepository.findByUser(user).stream()
                .map(p -> CustomerDetailDto.PaymentHistoryDto.builder()
                        .id(p.getId())
                        .amount(p.getAmount())
                        .paymentMethod(p.getPaymentMethod())
                        .transactionId(p.getTransactionId())
                        .status(p.getStatus())
                        .build())
                .collect(Collectors.toList()));

        return builder.build();
    }
}
