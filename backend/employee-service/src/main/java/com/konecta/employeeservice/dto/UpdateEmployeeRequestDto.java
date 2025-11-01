package com.konecta.employeeservice.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateEmployeeRequestDto {
    
    private String positionTitle;
    private BigDecimal salaryGross;
    private BigDecimal salaryNet;
    private Integer departmentId;
}
