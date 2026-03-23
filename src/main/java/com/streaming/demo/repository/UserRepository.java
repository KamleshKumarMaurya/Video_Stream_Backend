package com.streaming.demo.repository;

import com.streaming.demo.dto.UserDashboardResponse;
import com.streaming.demo.entity.Role;
import com.streaming.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	Optional<User> findByMobileNo(String mobileNo);

	boolean existsByEmail(String email);

	boolean existsByMobileNo(String mobileNo);

	Page<User> findByRole(Role role, Pageable pageable);

	@Query("""
			    SELECT new com.streaming.demo.dto.UserDashboardResponse(
			        COUNT(u),
			        SUM(CASE WHEN u.active = true THEN 1 ELSE 0 END),
			        SUM(CASE WHEN u.active = false THEN 1 ELSE 0 END),
			        SUM(CASE WHEN u.createdAt >= :last7Days THEN 1 ELSE 0 END)
			    )
			    FROM User u
			""")
	UserDashboardResponse getUserDashboard(@Param("last7Days") LocalDateTime last7Days);
}
