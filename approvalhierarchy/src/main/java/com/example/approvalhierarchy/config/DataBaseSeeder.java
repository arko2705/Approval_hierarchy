package com.example.approvalhierarchy.config;

import com.example.approvalhierarchy.model.Employee;
import com.example.approvalhierarchy.model.Role;
import com.example.approvalhierarchy.model.User;
import com.example.approvalhierarchy.repository.EmployeeRepository;
import com.example.approvalhierarchy.repository.RoleRepository;
import com.example.approvalhierarchy.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

//whenevr db cleaned,this will come in handy to create default admin
@Component
public class DataBaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataBaseSeeder(UserRepository userRepository, RoleRepository roleRepository,
            EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // If there are absolutely no users in the database, seed the initial Admin!
        if (userRepository.count() == 0) {

            // 1. Create the ADMIN role if it doesn't exist
            Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
                Role newRole = new Role();
                newRole.setName("ADMIN");
                return roleRepository.save(newRole);
            });

            // 2. Create a dummy Employee profile for the Admin
            Employee adminEmployee = new Employee();
            adminEmployee.setName("System Administrator");
            adminEmployee.setEmail("admin@company.com");
            adminEmployee.setPosition("IT Admin");
            adminEmployee.setDepartment("IT");
            employeeRepository.save(adminEmployee);

            // 3. Create the User account and link it
            User adminUser = new User();
            adminUser.setUsername("admin");
            // Encoding the default password: "admin"
            adminUser.setPassword(passwordEncoder.encode("admin"));
            adminUser.setEmail("admin@company.com");
            adminUser.setEmployee(adminEmployee);

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            adminUser.setRoles(roles);

            userRepository.save(adminUser);

            System.out.println("✅ DATABASE SEEDED: Default admin user created (Username: admin, Password: admin)");
        }
    }
}
