package com.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserAnalyticsDTO {
    private long totalUsers;
    private long totalMigrants;
    private long totalDoctors;
    private long totalNGOs;
    private long newUsersThisMonth;
}
