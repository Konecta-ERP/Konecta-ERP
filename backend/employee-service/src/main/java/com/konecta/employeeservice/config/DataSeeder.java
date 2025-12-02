package com.konecta.employeeservice.config;

import com.konecta.employeeservice.client.IdentityClient;
import com.konecta.employeeservice.entity.Department;
import com.konecta.employeeservice.entity.Employee;
import com.konecta.employeeservice.repository.DepartmentRepository;
import com.konecta.employeeservice.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Component
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final IdentityClient identityClient;

    public DataSeeder(DepartmentRepository departmentRepository,
                      EmployeeRepository employeeRepository,
                      IdentityClient identityClient) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
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
        createFullEmployeeFlow(
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
        createFullEmployeeFlow(
                "sw_eng@email.com", "Sarah", "Connor", "+01234567896", "EMP", "password",
                "Software Engineer", new BigDecimal("90000.00"), new BigDecimal("65000.00"), engineeringDept
        );

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
}