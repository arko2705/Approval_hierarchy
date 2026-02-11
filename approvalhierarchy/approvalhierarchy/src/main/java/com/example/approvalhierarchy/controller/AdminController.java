package com.example.approvalhierarchy.controller;

import com.example.approvalhierarchy.model.Employee;
import com.example.approvalhierarchy.model.Role;
import com.example.approvalhierarchy.model.User;
import com.example.approvalhierarchy.repository.RoleRepository;
import com.example.approvalhierarchy.service.EmployeeService;
import com.example.approvalhierarchy.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final EmployeeService employeeService;

    public AdminController(UserService userService, RoleRepository roleRepository, EmployeeService employeeService) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.employeeService = employeeService;
    }

    @GetMapping
    public String adminDashboard(Model model) {
        // Get all users
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        
        return "admin/dashboard";
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id).orElse(null);
        if (user == null) {
            return "redirect:/admin?error=usernotfound";
        }
        
        List<Role> roles = roleRepository.findAll();
        List<Employee> employees = employeeService.getAllEmployees();
        
        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        model.addAttribute("employees", employees);
        
        return "admin/edit-user";
    }

    @PostMapping("/users/{id}/update")
    public String updateUser(@PathVariable Long id, 
                             @ModelAttribute User user,
                             @RequestParam(value = "roleIds", required = false) List<Long> roleIds,
                             @RequestParam(value = "employeeId", required = false) Long employeeId) {
        User existingUser = userService.getUserById(id).orElse(null);
        if (existingUser == null) {
            return "redirect:/admin?error=usernotfound";
        }
        
        // Update basic info
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        
        // Update roles if provided
        if (roleIds != null && !roleIds.isEmpty()) {
            existingUser.getRoles().clear();
            roleRepository.findAllById(roleIds).forEach(existingUser::addRole);
        }
        
        // Update employee link if provided
        if (employeeId != null) {
            Employee employee = employeeService.getEmployeeById(employeeId).orElse(null);
            existingUser.setEmployee(employee);
        } else {
            existingUser.setEmployee(null);
        }
        
        userService.updateUser(existingUser);
        return "redirect:/admin?success=userupdated";
    }

    @GetMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin?success=userdeleted";
    }
}