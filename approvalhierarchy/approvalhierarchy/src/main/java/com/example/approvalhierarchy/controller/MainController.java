package com.example.approvalhierarchy.controller;

import com.example.approvalhierarchy.model.Employee;
import com.example.approvalhierarchy.model.User;
import com.example.approvalhierarchy.service.EmployeeService;
import com.example.approvalhierarchy.service.RequestService;
import com.example.approvalhierarchy.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    private final EmployeeService employeeService;
    private final RequestService requestService;
    private final UserService userService;

    public MainController(EmployeeService employeeService, RequestService requestService, UserService userService) {
        this.employeeService = employeeService;
        this.requestService = requestService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Add authentication info
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // Get current user
        User currentUser = userService.getUserByUsername(username).orElse(null);
        model.addAttribute("currentUser", currentUser);
        
        // Check if user is linked to an employee
        Employee currentEmployee = null;
        if (currentUser != null && currentUser.getEmployee() != null) {
            currentEmployee = currentUser.getEmployee();
            model.addAttribute("currentEmployee", currentEmployee);
        }
        
        // Add employee count
        long employeeCount = employeeService.getEmployeeCount();
        model.addAttribute("employeeCount", employeeCount);
        
        // Add pending requests count
        long pendingRequestCount = requestService.getPendingRequestCount();
        model.addAttribute("pendingRequestCount", pendingRequestCount);
        
        // If the user is a manager, get pending requests for approval
        if (currentEmployee != null) {
            long pendingForUser = requestService.getPendingRequestsForApproverCount(currentEmployee.getId());
            model.addAttribute("pendingForUser", pendingForUser);
        }
        
        return "index";
    }
}