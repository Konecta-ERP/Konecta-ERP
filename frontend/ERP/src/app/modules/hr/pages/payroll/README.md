# Payroll Details Feature

## Overview

The Payroll Details page allows HR staff to view detailed payroll information for any employee for a specific month. The feature includes employee search functionality and displays comprehensive payroll breakdowns.

## Location

-   **Route**: `/home/hr/payroll`
-   **Module**: HR Module
-   **Navigation**: Available in the HR sidebar as "Payroll"

## Files Created

### Frontend

1. **Component Files**:

    - `frontend/ERP/src/app/modules/hr/pages/payroll/payroll.ts` - Component logic
    - `frontend/ERP/src/app/modules/hr/pages/payroll/payroll.html` - Template
    - `frontend/ERP/src/app/modules/hr/pages/payroll/payroll.css` - Styles
    - `frontend/ERP/src/app/modules/hr/pages/payroll/mock-payroll-data.json` - Mock data for testing

2. **Service**:

    - `frontend/ERP/src/app/core/services/payroll.service.ts` - Payroll API service

3. **Interfaces**:

    - `frontend/ERP/src/app/core/interfaces/iPayrollDetail.ts` - Payroll detail item interface
    - `frontend/ERP/src/app/core/interfaces/iPayrollSummary.ts` - Payroll summary interface

4. **Route Configuration**:
    - Updated `frontend/ERP/src/app/modules/hr/hr.routes.ts` to include payroll route
    - Updated `frontend/ERP/src/app/modules/hr/hr-layout/hr-layout.ts` to add sidebar navigation

## Features

### Employee Search

-   Search by name, position, or department
-   Uses the existing employee search infrastructure
-   Displays results in card format matching the app's design system

### Payroll Query

-   Select an employee from search results
-   Choose a month using a month picker (defaults to current month)
-   Fetch payroll details with a single click

### Payroll Display

-   **Summary Cards**: Display basic pay, overtime, deductions, and net pay
-   **Detailed Breakdown**:
    -   Basic Pay Details (work days, paid leave)
    -   Overtime Details (weekend work, extra hours)
    -   Deductions (unauthorized absences, half-day deductions, unpaid leave)
-   Currency formatted amounts
-   Color-coded sections for easy reading

## API Integration

### Endpoint

```
GET /api/employees/{employeeId}/payroll
```

### Request Body

```json
{
    "yearMonth": "YYYY-MM"
}
```

### Response Structure

```json
{
    "status": 200,
    "data": {
        "employeeId": 2,
        "yearMonth": "2025-11",
        "basicPay": 4200.0,
        "overtimePay": 150.0,
        "deductions": 100.0,
        "netPay": 4250.0,
        "details": [
            {
                "type": "BASIC|OVERTIME|DEDUCTION",
                "description": "Description text",
                "amount": 161.54
            }
        ]
    },
    "message": "Payroll retrieved.",
    "cMessage": "Payroll for employee 2 for 2025-11"
}
```

## UI/UX Design

### Design Principles

-   Follows existing app patterns from employees and recruitment pages
-   Uses PrimeNG components (p-card, p-button, p-select, p-tag)
-   Gradient headers matching app theme
-   Responsive grid layout
-   Clear visual hierarchy with icons

### Color Scheme

-   Blue: Basic pay and primary actions
-   Purple: Overtime
-   Red: Deductions
-   Green: Net pay and success states

### Workflow

1. **Search Phase**: User searches for employees with filters
2. **Selection Phase**: User clicks on an employee card
3. **Query Phase**: User selects a month and clicks "Get Payroll Details"
4. **Display Phase**: Payroll summary and breakdown are shown

## Testing with Mock Data

The `mock-payroll-data.json` file contains realistic payroll data that matches the backend response structure. It includes:

-   Basic pay entries for work days
-   Overtime entries for extra hours and weekend work
-   Deductions for absences and half-days
-   Paid leave entries

To use mock data during development, you can temporarily modify the service to return this data instead of making API calls.

## Code Style

The implementation follows the existing frontend patterns:

-   Angular standalone components
-   Observable-based async operations
-   SharedModule imports
-   MessageService for user feedback
-   NgxSpinner for loading states
-   Type-safe interfaces
-   Consistent naming conventions

## Future Enhancements

-   Export payroll to PDF
-   Compare multiple months
-   Payroll history timeline
-   Filter by date range
-   Download payroll report
