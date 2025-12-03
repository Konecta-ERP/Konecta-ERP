package com.konecta.employeeservice.config;

import com.konecta.employeeservice.client.IdentityClient;
import com.konecta.employeeservice.entity.AttendanceRecord;
import com.konecta.employeeservice.entity.Department;
import com.konecta.employeeservice.entity.Employee;
import com.konecta.employeeservice.entity.LeaveRequest;
import com.konecta.employeeservice.model.enums.RequestStatus;
import com.konecta.employeeservice.model.enums.RequestType;
import com.konecta.employeeservice.repository.AttendanceRecordRepository;
import com.konecta.employeeservice.repository.DepartmentRepository;
import com.konecta.employeeservice.repository.EmployeeRepository;
import com.konecta.employeeservice.repository.LeaveRequestRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

@Component
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final IdentityClient identityClient;

    public DataSeeder(DepartmentRepository departmentRepository,
                      EmployeeRepository employeeRepository,
                      AttendanceRecordRepository attendanceRecordRepository,
                      LeaveRequestRepository leaveRequestRepository,
                      IdentityClient identityClient) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.identityClient = identityClient;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n--- Starting Orchestrated Data Seeder (Employee Service) ---");

        Department hrDept = createDepartmentIfNotExist("Human Resources");
        Department financeDept = createDepartmentIfNotExist("Finance");
        Department engineeringDept = createDepartmentIfNotExist("Engineering");

        System.out.println("\n--- Creating ADMIN User ---");
        // ADMIN (Retains descriptive names for system roles)
        Employee adminUser = createFullEmployeeFlow(
                "admin@email.com", "System", "Admin", "+00000000000", "ADMIN", "password",
                "System Administrator", new BigDecimal("120000.00"), new BigDecimal("80000.00"), hrDept
        );

        System.out.println("\n--- Creating Operational Users (Randomized Names) ---");

        // --- HR Manager ---
        Employee hrManager = createFullEmployeeFlow(
                "hr_manager@email.com", "Jane", "Smith", "+01234567892", "HR_MANAGER", "password",
                "HR Manager", new BigDecimal("80000.00"), new BigDecimal("55000.00"), hrDept
        );
        assignManagerToDepartment(hrDept, hrManager);

        // --- HR Associate ---
        Employee hrAssociate = createFullEmployeeFlow(
                "hr_associate@email.com", "Robert", "Jones", "+01234567891", "HR_ASSOCIATE", "password",
                "HR Associate", new BigDecimal("50000.00"), new BigDecimal("35000.00"), hrDept
        );

        // --- CFO ---
        Employee cfo = createFullEmployeeFlow(
                "cfo@email.com", "Martha", "King", "+01234567894", "CFO", "password",
                "Chief Financial Officer", new BigDecimal("150000.00"), new BigDecimal("100000.00"), financeDept
        );
        assignManagerToDepartment(financeDept, cfo);

        // --- Accountant ---
        createFullEmployeeFlow(
                "accountant@email.com", "David", "Miller", "+01234567893", "ACCOUNTANT", "password",
                "Junior Accountant", new BigDecimal("60000.00"), new BigDecimal("40000.00"), financeDept
        );

        // --- Engineering Manager (MANAGER) ---
        Employee engManager = createFullEmployeeFlow(
                "eng_manager@email.com", "John", "Doe", "+01234567895", "MANAGER", "password",
                "Lead Software Engineer", new BigDecimal("130000.00"), new BigDecimal("90000.00"), engineeringDept
        );
        assignManagerToDepartment(engineeringDept, engManager);

        // --- Software Engineer (EMP) ---
        Employee softwareEngineer = createFullEmployeeFlow(
                "sw_eng@email.com", "Sarah", "Connor", "+01234567896", "EMP", "password",
                "Software Engineer", new BigDecimal("90000.00"), new BigDecimal("65000.00"), engineeringDept
        );

        System.out.println("\n--- Seeding Attendance and Leave Data for Payroll ---");
        
        // Seed attendance and leave data for October and November 2024
        if (hrManager != null) {
            seedPayrollDataForEmployee(hrManager, "Jane Smith (HR Manager)");
        }
        if (hrAssociate != null) {
            seedPayrollDataForEmployee(hrAssociate, "Robert Jones (HR Associate)");
        }
        if (softwareEngineer != null) {
            seedPayrollDataForEmployee(softwareEngineer, "Sarah Connor (Software Engineer)");
        }

        System.out.println("--- Data Seeder Complete ---");
    }

    // --- Helper Methods ---

    private Department createDepartmentIfNotExist(String name) {
        return departmentRepository.findByName(name)
                .orElseGet(() -> {
                    Department newDepartment = new Department();
                    newDepartment.setName(name);
                    Department saved = departmentRepository.save(newDepartment);
                    System.out.println("Department created: " + name);
                    return saved;
                });
    }

    private Employee createFullEmployeeFlow(
            String email, String firstName, String lastName, String phone, String role, String password,
            String positionTitle, BigDecimal salaryGross, BigDecimal salaryNet, Department department
    ) {
        UUID userId = createUserInIdentityService(email, firstName, lastName, phone, role, password);

        if (userId == null) {
            System.err.println("Skipping local employee creation for " + email + " (User creation failed or exists)");
            return null;
        }

        if (employeeRepository.existsByUserId(userId)) {
            return employeeRepository.findByUserId(userId).orElse(null);
        }

        Employee employee = new Employee();
        employee.setUserId(userId);
        employee.setPositionTitle(positionTitle);
        employee.setHireDate(LocalDate.now());
        employee.setSalaryGross(salaryGross);
        employee.setSalaryNet(salaryNet);
        employee.setDepartment(department);

        Employee savedEmployee = employeeRepository.save(employee);
        System.out.println("Employee created locally for: " + email);
        return savedEmployee;
    }

    private UUID createUserInIdentityService(String email, String firstName, String lastName, String phone, String role, String password) {
        Map<String, Object> userRequest = Map.of(
                "email", email,
                "firstName", firstName,
                "lastName", lastName,
                "phone", phone,
                "role", role,
                "password", password
        );

        return identityClient.createUser(userRequest);
    }

    private void assignManagerToDepartment(Department dept, Employee manager) {
        if (manager != null) {
            dept.setManager(manager);
            departmentRepository.save(dept);
            System.out.println("Manager assigned to " + dept.getName());
        }
    }

    /**
     * Seeds comprehensive attendance and leave data for an employee for October and November 2025.
     * Creates diverse scenarios including regular work days, overtime, weekends, leaves, and absences.
     */
    private void seedPayrollDataForEmployee(Employee employee, String employeeName) {
        System.out.println("Seeding payroll data for: " + employeeName);
        
        // October 2025 - More regular with some variations
        seedOctoberData(employee);
        
        // November 2025 - More diverse with leaves, overtime, and absences
        seedNovemberData(employee);
    }

    private void seedOctoberData(Employee employee) {
        LocalDate startDate = LocalDate.of(2025, 10, 1);
        LocalDate endDate = LocalDate.of(2025, 10, 31);
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            int dayOfWeek = date.getDayOfWeek().getValue(); // Monday=1, Sunday=7
            
            // Work week: Sunday-Thursday (7, 1-4)
            // Weekend: Friday-Saturday (5-6)
            
            if (dayOfWeek == 5 || dayOfWeek == 6) {
                // Weekend - occasionally work on weekends for overtime
                // Oct 2025: Fridays=3,10,17,24,31  Saturdays=4,11,18,25
                if (date.getDayOfMonth() == 4 || date.getDayOfMonth() == 18) {
                    // Weekend work (overtime scenario)
                    createAttendance(employee, date, 
                        LocalTime.of(10, 0), LocalTime.of(14, 30)); // 4.5 hours
                }
                continue; // Skip other weekends
            }
            
            // Approved vacation leave Oct 15-16 (Thursday-Friday)
            if (date.getDayOfMonth() >= 15 && date.getDayOfMonth() <= 16) {
                if (date.getDayOfMonth() == 15) {
                    createLeaveRequest(employee, 
                        LocalDate.of(2025, 10, 15), 
                        LocalDate.of(2025, 10, 16), 
                        RequestType.VACATION, 
                        "Family vacation", 
                        RequestStatus.APPROVED);
                }
                continue; // No attendance on leave days
            }
            
            // Regular work day - mostly 8 hours
            if (date.getDayOfMonth() == 8 || date.getDayOfMonth() == 22) {
                // Overtime days - 9-10 hours (Wed, Wed)
                createAttendance(employee, date, 
                    LocalTime.of(8, 30), LocalTime.of(18, 30)); // 10 hours
            } else if (date.getDayOfMonth() == 29) {
                // Half day (Wednesday)
                createAttendance(employee, date, 
                    LocalTime.of(8, 30), LocalTime.of(13, 0)); // 4.5 hours
            } else {
                // Normal 8-hour day
                createAttendance(employee, date, 
                    LocalTime.of(8, 30), LocalTime.of(17, 30)); // 9 hours with 1 hour lunch
            }
        }
    }

    private void seedNovemberData(Employee employee) {
        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 11, 30);
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            int dayOfWeek = date.getDayOfWeek().getValue();
            
            // Weekend check
            if (dayOfWeek == 5 || dayOfWeek == 6) {
                // More frequent weekend work in November
                // Nov 2025: Fridays=7,14,21,28  Saturdays=1,8,15,22,29
                if (date.getDayOfMonth() == 1 || date.getDayOfMonth() == 8 || date.getDayOfMonth() == 22) {
                    createAttendance(employee, date, 
                        LocalTime.of(9, 0), LocalTime.of(15, 0)); // 6 hours overtime
                }
                continue;
            }
            
            // Sick leave Nov 5-6 (Wednesday-Thursday)
            if (date.getDayOfMonth() >= 5 && date.getDayOfMonth() <= 6) {
                if (date.getDayOfMonth() == 5) {
                    createLeaveRequest(employee, 
                        LocalDate.of(2025, 11, 5), 
                        LocalDate.of(2025, 11, 6), 
                        RequestType.SICK, 
                        "Flu symptoms", 
                        RequestStatus.APPROVED);
                }
                continue;
            }
            
            // Unpaid leave Nov 18 (Tuesday)
            if (date.getDayOfMonth() == 18) {
                createLeaveRequest(employee, 
                    LocalDate.of(2025, 11, 18), 
                    LocalDate.of(2025, 11, 18), 
                    RequestType.UNPAID, 
                    "Personal matters", 
                    RequestStatus.APPROVED);
                continue;
            }
            
            // Unauthorized absence (no clock-in, no leave request) on Nov 13 (Thu) and 27 (Thu)
            if (date.getDayOfMonth() == 13 || date.getDayOfMonth() == 27) {
                continue; // Skip - no attendance record = unauthorized absence
            }
            
            // Half day on Nov 12 (Wednesday - only morning)
            if (date.getDayOfMonth() == 12) {
                createAttendance(employee, date, 
                    LocalTime.of(8, 30), LocalTime.of(12, 30)); // 4 hours
                continue;
            }
            
            // Extended overtime days (Tue, Wed, Tue)
            if (date.getDayOfMonth() == 4 || date.getDayOfMonth() == 19 || date.getDayOfMonth() == 25) {
                createAttendance(employee, date, 
                    LocalTime.of(8, 0), LocalTime.of(19, 30)); // 11.5 hours (includes lunch)
            } 
            // Moderate overtime (Sun, Thu)
            else if (date.getDayOfMonth() == 9 || date.getDayOfMonth() == 20) {
                createAttendance(employee, date, 
                    LocalTime.of(8, 30), LocalTime.of(18, 0)); // 9.5 hours
            }
            // Regular days
            else {
                createAttendance(employee, date, 
                    LocalTime.of(8, 30), LocalTime.of(17, 30)); // 9 hours with lunch
            }
        }
    }

    private void createAttendance(Employee employee, LocalDate date, LocalTime clockIn, LocalTime clockOut) {
        AttendanceRecord record = new AttendanceRecord();
        record.setEmployee(employee);
        record.setDate(date);
        record.setClockInTime(LocalDateTime.of(date, clockIn));
        record.setClockOutTime(LocalDateTime.of(date, clockOut));
        attendanceRecordRepository.save(record);
    }

    private void createLeaveRequest(Employee employee, LocalDate startDate, LocalDate endDate, 
                                    RequestType type, String reason, RequestStatus status) {
        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leave.setRequestType(type);
        leave.setReason(reason);
        leave.setStatus(status);
        leaveRequestRepository.save(leave);
    }
}