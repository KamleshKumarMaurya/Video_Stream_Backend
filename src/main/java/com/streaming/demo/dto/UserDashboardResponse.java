package com.streaming.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Data
public class UserDashboardResponse {

    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long newUsersLast7Days;
}
