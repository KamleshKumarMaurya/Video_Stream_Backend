package com.streaming.demo.repository;

import com.streaming.demo.entity.Role;
import com.streaming.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMobileNo(String mobileNo);
    boolean existsByEmail(String email);
    boolean existsByMobileNo(String mobileNo);
    Page<User> findByRole(Role role, Pageable pageable);
}
