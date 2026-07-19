package com.example.approvalhierarchy.controller;

// import com.example.approvalhierarchy.model.Employee;
// import com.example.approvalhierarchy.model.User;
import com.example.approvalhierarchy.service.EmployeeService;
import com.example.approvalhierarchy.service.UserService;
import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.ModelAttribute;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
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
    public String login(Authentication auth) {// spring will automatically give current user auth if asked
        // If we already have a user, send them straight home
        if (auth != null && auth.isAuthenticated()) {
            return "redirect:/";
        }

        return "auth/login";
    }

    // @GetMapping("/register") // show the empty signup page
    // public String showRegistrationForm(Model model) {// spring provides an empty
    // model(a hashmap)
    // model.addAttribute("user", new User());// controller fills model up with an
    // empty user,so it can fill data in
    // // this and save it

    // List<Employee> employees = employeeService.getAllEmployees();
    // model.addAttribute("employees", employees);// for dropdown
    // // roles are hardcoded as they wouldnt change(assumed),while employees are
    // // dynamic
    // return "auth/register";
    // }

    // @PostMapping("/register") // process the submitted fields and persist the new
    // user
    // public String registerUser(@ModelAttribute("user") User user,
    // @RequestParam("role") String role,
    // @RequestParam(value = "employeeId", required = false) Long employeeId, //
    // link to employee
    // Model model) {

    // if (userService.existsByUsername(user.getUsername())) {
    // model.addAttribute("error", "Username is already taken!");
    // model.addAttribute("employees", employeeService.getAllEmployees());// being
    // added during re register after
    // // error,else drop down will be empty
    // return "auth/register";// by default spring goes from here to template,to
    // ssearch auth/regiter.html
    // }

    // if (userService.existsByEmail(user.getEmail())) {
    // model.addAttribute("error", "Email is already in use!");
    // model.addAttribute("employees", employeeService.getAllEmployees());
    // return "auth/register";
    // }

    // // Link user to employee if employeeId is provided
    // if (employeeId != null) {
    // Employee employee = employeeService.getEmployeeById(employeeId)
    // .orElse(null);
    // user.setEmployee(employee);
    // }

    // userService.registerNewUser(user, role);
    // return "redirect:/login?registered";
    // }
}
