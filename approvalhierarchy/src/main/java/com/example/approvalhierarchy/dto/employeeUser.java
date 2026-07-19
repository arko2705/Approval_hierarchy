package com.example.approvalhierarchy.dto;

import com.example.approvalhierarchy.model.Employee;

public class employeeUser {

    // 1. The Employee piece
    private Employee employee;

    // 2. The Login piece
    private String username;
    private String password;
    private String role;

    // --- Constructor ---
    public employeeUser() {
        this.employee = new Employee();
    }

    // --- Getters and Setters ---
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
// A DTO (Data Transfer Object) is just a temporary, custom box we use to carry
// data from the HTML web page to your backend.

// Why do we need it here? Usually, you submit a form and Spring drops the data
// right into a User model or an Employee model. But our new form is collecting
// both at the same time (Username + Password + Employee Name + Department).

// Since neither the User class nor the Employee class has all those fields, we
// create a temporary DTO class that has all of them.

// The frontend puts all the data into the DTO box, sends it to the backend, and
// then our controller unpacks the box and saves the login info to the users
// table and the employee info to the employees table!