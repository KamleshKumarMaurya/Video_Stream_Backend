package com.streaming.demo.repository;

import com.streaming.demo.entity.Subscription;
import com.streaming.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findFirstByUserOrderByExpiryDateDesc(User user);
}
