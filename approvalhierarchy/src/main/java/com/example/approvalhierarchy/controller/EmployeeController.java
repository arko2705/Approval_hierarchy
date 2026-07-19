package com.example.approvalhierarchy.controller;

import com.example.approvalhierarchy.model.Employee;
import com.example.approvalhierarchy.service.EmployeeService;
import com.example.approvalhierarchy.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final UserService userService;

    @Autowired
    public EmployeeController(EmployeeService employeeService, UserService userService) {

        this.employeeService = employeeService;
        this.userService = userService;
    }

    @GetMapping
    public String listEmployees(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        return "employee/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        // Send our empty DTO box to the frontend instead of just an Employee
        model.addAttribute("dto", new com.example.approvalhierarchy.dto.employeeUser());
        model.addAttribute("managers", employeeService.getAllEmployees());
        return "employee/form";
    }

    @PostMapping("/add")
    public String addEmployee(@ModelAttribute("dto") com.example.approvalhierarchy.dto.employeeUser dto,
            Model model,
            RedirectAttributes attributes) {

        // 1. Save the Employee part to the database first
        Employee savedEmployee = employeeService.saveEmployee(dto.getEmployee());

        // 2. Create the User login part
        com.example.approvalhierarchy.model.User newUser = new com.example.approvalhierarchy.model.User();
        newUser.setUsername(dto.getUsername());
        newUser.setPassword(dto.getPassword());
        // For simplicity, using the same email as the employee
        newUser.setEmail(savedEmployee.getEmail());

        // 3. Link them together!
        newUser.setEmployee(savedEmployee);

        // 4. Save the user with their role
        userService.registerNewUser(newUser, dto.getRole());

        attributes.addFlashAttribute("success", "Employee & Login Account created successfully!");
        return "redirect:/employees";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes attributes) {
        Optional<Employee> employeeOpt = employeeService.getEmployeeById(id);

        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            // Prepare DTO for the form binding
            com.example.approvalhierarchy.dto.employeeUser dto = new com.example.approvalhierarchy.dto.employeeUser();
            dto.setEmployee(employee);
            
            // Fill username and role from existing user
            com.example.approvalhierarchy.model.User user = employee.getUserAccount();
            if (user != null) {
                dto.setUsername(user.getUsername());
                if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                    dto.setRole(user.getRoles().iterator().next().getName());
                }
            }
            
            model.addAttribute("dto", dto);
            model.addAttribute("managers", employeeService.getAllEmployees());
            return "employee/form";
        } else {
            attributes.addFlashAttribute("error", "Employee not found!");
            return "redirect:/employees";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateEmployee(@PathVariable Long id,
            @ModelAttribute("dto") com.example.approvalhierarchy.dto.employeeUser dto,
            RedirectAttributes attributes) {
        Optional<Employee> existingOpt = employeeService.getEmployeeById(id);
        if (!existingOpt.isPresent()) {
            attributes.addFlashAttribute("error", "Employee not found!");
            return "redirect:/employees";
        }
        
        Employee existingEmployee = existingOpt.get();
        // Update employee fields
        existingEmployee.setName(dto.getEmployee().getName());
        existingEmployee.setEmail(dto.getEmployee().getEmail());
        existingEmployee.setPosition(dto.getEmployee().getPosition());
        existingEmployee.setDepartment(dto.getEmployee().getDepartment());
        existingEmployee.setManager(dto.getEmployee().getManager());
        
        employeeService.saveEmployee(existingEmployee);
        
        // Update user fields if a user exists
        com.example.approvalhierarchy.model.User user = existingEmployee.getUserAccount();
        if (user != null) {
            user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmployee().getEmail());
            userService.updateUserDetails(user, dto.getPassword(), dto.getRole());
        }
        
        attributes.addFlashAttribute("success", "Employee and User details updated successfully!");
        return "redirect:/employees";
    }

    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes attributes) {
        boolean hasSubordinates = employeeService.hasSubordinates(id);

        if (hasSubordinates) {
            attributes.addFlashAttribute("error", "Cannot delete an employee who has subordinates!");// in spring mvc it
                                                                                                     // is for frontend.
                                                                                                     // Check line 26 in
                                                                                                     // employee/list.html
            return "redirect:/employees";
        }

        employeeService.deleteEmployee(id);
        attributes.addFlashAttribute("success", "Employee deleted successfully!");
        return "redirect:/employees";// When you return "redirect:/employees", you are not pointing directly to an
                                     // HTML file.Instead, you are telling the user's web browser: "I'm done saving
                                     // this data. Now, make a brand new request to the URL
                                     // http://localhost:8080/employees."
    }

    @GetMapping("/subordinates/{managerId}")
    public String viewSubordinates(@PathVariable Long managerId, Model model) {
        Optional<Employee> managerOpt = employeeService.getEmployeeById(managerId);

        if (managerOpt.isPresent()) {
            List<Employee> subordinates = employeeService.getSubordinates(managerId);
            model.addAttribute("manager", managerOpt.get());
            model.addAttribute("subordinates", subordinates);
            return "employee/subordinates";
        } else {
            return "redirect:/employees";
        }
    }

    @GetMapping("/hierarchy")
    public String viewHierarchy(Model model) {
        List<Employee> orgHierarchy = employeeService.getOrganizationalHierarchy();
        model.addAttribute("hierarchy", orgHierarchy);
        return "employee/hierarchy";
    }
}