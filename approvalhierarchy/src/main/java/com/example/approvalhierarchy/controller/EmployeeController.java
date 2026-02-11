package com.example.approvalhierarchy.controller;

import com.example.approvalhierarchy.model.Employee;
import com.example.approvalhierarchy.service.EmployeeService;
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
    
    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    @GetMapping
    public String listEmployees(Model model) {
        List<Employee> employees = employeeService.getAllEmployees();
        model.addAttribute("employees", employees);
        return "employee/list";
    }
    
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("managers", employeeService.getAllEmployees());
        return "employee/form";
    }
    
    @PostMapping("/add")
    public String addEmployee(@Valid @ModelAttribute("employee") Employee employee, 
                             BindingResult result, 
                             Model model,
                             RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("managers", employeeService.getAllEmployees());
            return "employee/form";
        }
        
        employeeService.saveEmployee(employee);
        attributes.addFlashAttribute("success", "Employee added successfully!");
        return "redirect:/employees";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes attributes) {
        Optional<Employee> employeeOpt = employeeService.getEmployeeById(id);
        
        if (employeeOpt.isPresent()) {
            model.addAttribute("employee", employeeOpt.get());
            model.addAttribute("managers", employeeService.getAllEmployees());
            return "employee/form";
        } else {
            attributes.addFlashAttribute("error", "Employee not found!");
            return "redirect:/employees";
        }
    }
    
    @PostMapping("/edit/{id}")
    public String updateEmployee(@PathVariable Long id,
                               @Valid @ModelAttribute("employee") Employee employee,
                               BindingResult result,
                               Model model,
                               RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("managers", employeeService.getAllEmployees());
            return "employee/form";
        }
        
        employeeService.saveEmployee(employee);
        attributes.addFlashAttribute("success", "Employee updated successfully!");
        return "redirect:/employees";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes attributes) {
        boolean hasSubordinates = employeeService.hasSubordinates(id);
        
        if (hasSubordinates) {
            attributes.addFlashAttribute("error", "Cannot delete an employee who has subordinates!");
            return "redirect:/employees";
        }
        
        employeeService.deleteEmployee(id);
        attributes.addFlashAttribute("success", "Employee deleted successfully!");
        return "redirect:/employees";
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