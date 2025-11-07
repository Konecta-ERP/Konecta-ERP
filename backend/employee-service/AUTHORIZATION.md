# Employee Service — Endpoint Authorization

This document summarizes the authorization rules for endpoints in the `employee-service` controllers.

Format: HTTP_METHOD PATH -> Authorization / Notes

## EmployeeController (`/employees`)

- GET /employees/{id} -> Authenticated. Managers and Admins may access any employee. Other users may access only their own employee record (JWT `userId` must match employee.userId).
- GET /employees/by-user/{userId} -> Authenticated. Only the user themselves (JWT `userId` must equal path `userId`).
- POST /employees -> Requires MANAGER or ADMIN authority.
- GET /employees/search -> Requires MANAGER or ADMIN authority.
- PATCH /employees/{id} -> Requires MANAGER or ADMIN authority.

## AttendanceController

- POST /employees/{id}/clock-in -> Authenticated. Only the employee themselves: JWT `userId` must match employee.userId. If employee id does not exist, controller returns Access Denied to avoid enumeration.
- POST /employees/{id}/clock-out -> Authenticated. Only the employee themselves: JWT `userId` must match employee.userId. If employee id does not exist, controller returns Access Denied to avoid enumeration.

## DepartmentController (`/departments`)

- POST /departments -> Requires ADMIN authority.
- GET /departments -> Authenticated (any authenticated user).
- GET /departments/{id} -> Authenticated (any authenticated user).
- PUT /departments/{id} -> Requires ADMIN or MANAGER authority.
- DELETE /departments/{id} -> Requires ADMIN authority.

## PerformanceController

- POST /employees/{id}/goals -> Requires MANAGER authority.
- POST /employees/{id}/feedback -> Requires MANAGER or HR_EMP authority.
- GET /employees/{id}/goals -> Authenticated. Managers/Admins may fetch any employee's goals. Other users may fetch only their own goals (JWT `userId` must match employee.userId).
- GET /employees/{id}/feedback -> No explicit authorization annotation (caller allowed as-is). Consider restricting to Manager/Admin or owner if needed.
- DELETE /goals/{id} -> No explicit authorization annotation (caller allowed as-is). Consider restricting.
- DELETE /feedback/{id} -> No explicit authorization annotation (caller allowed as-is). Consider restricting.

## LeaveController

- POST /employees/{employeeId}/leave-requests -> Authenticated. Only the employee themselves may submit a leave request (JWT `userId` must match employee.userId).
- PATCH /leave-requests/{requestId}/status -> Requires MANAGER authority.
- GET /employees/{employeeId}/leave-balance -> Authenticated. Managers/Admins may view any balance. Other users may view only their own balance (JWT `userId` must match employee.userId).
- GET /employees/{employeeId}/leave-requests -> Authenticated. Managers/Admins may view any employee's requests. Other users may view only their own requests (JWT `userId` must match employee.userId).
- GET /leave-requests/{requestId} -> Authenticated. Managers/Admins may view any request. Other users may view only their own requests (ownership determined by the request's employeeId and compared to caller's JWT `userId`).
- DELETE /leave-requests/{requestId} -> Authenticated. Only the employee who owns the request may delete it (ownership determined by the request's employeeId).

## OffboardingController

- POST /employees/{employeeId}/offboard -> Requires ADMIN authority.
- GET /employees/{employeeId}/offboarding-checklist -> Requires ADMIN authority.
- PUT /offboarding-checklists/{checklistId} -> Requires ADMIN authority.

