package com.konecta.employeeservice.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class LeaveBalanceDto {
    private Integer employeeId;
    private int vacationDaysTaken;
    private int vacationDaysRemaining;
    private int sickDaysTaken;
    // ... add any other balances to track
}
