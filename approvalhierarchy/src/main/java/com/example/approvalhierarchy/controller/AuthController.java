package com.example.approvalhierarchy.controller;

import com.example.approvalhierarchy.model.Employee;
import com.example.approvalhierarchy.model.User;
import com.example.approvalhierarchy.service.EmployeeService;
import com.example.approvalhierarchy.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AuthController {

    private final UserService userService;
    private final EmployeeService employeeService;

    public AuthController(UserService userService, EmployeeService employeeService) {
        this.userService = userService;
        this.employeeService = employeeService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        
        // Get all employees to link with user account
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, 
                               @RequestParam("role") String role,
                               @RequestParam(value = "employeeId", required = false) Long employeeId,
                               Model model) {
        
        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("error", "Username is already taken!");
            model.addAttribute("employees", employeeService.getAllEmployees());
            return "auth/register";
        }
        
        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email is already in use!");
            model.addAttribute("employees", employeeService.getAllEmployees());
            return "auth/register";
        }
        
        // Link user to employee if employeeId is provided
        if (employeeId != null) {
            Employee employee = employeeService.getEmployeeById(employeeId)
                    .orElse(null);
            user.setEmployee(employee);
        }
        
        userService.registerNewUser(user, role);
        return "redirect:/login?registered";
    }
}