package com.streaming.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Column(unique = true)
	private String email;

	@Column(unique = true)
	private String mobileNo;

	@com.fasterxml.jackson.annotation.JsonIgnore
	private String password;

	@Enumerated(EnumType.STRING)
	private Role role;

	private boolean subscriptionActive;

	@Builder.Default
	private boolean active = true;
	@Builder.Default
	private boolean isTrialPlanUsed = false;
	private LocalDateTime subscriptionExpiry;
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

}
