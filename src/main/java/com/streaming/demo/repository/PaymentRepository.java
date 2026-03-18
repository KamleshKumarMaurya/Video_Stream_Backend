package com.streaming.demo.repository;

import com.streaming.demo.entity.Payment;
import com.streaming.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUser(User user);
}
