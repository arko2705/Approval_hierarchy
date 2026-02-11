package com.example.approvalhierarchy.service;

import com.example.approvalhierarchy.model.Employee;
import com.example.approvalhierarchy.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    
    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    public long getEmployeeCount() {
        return employeeRepository.count();
    }

    
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }
    
    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }
    
    @Transactional
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }
    
    @Transactional
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
    
    public List<Employee> getSubordinates(Long managerId) {
        return employeeRepository.findByManagerId(managerId);
    }
    
    public List<Employee> getTopLevelManagers() {
        return employeeRepository.findByManagerIsNull();
    }
    
    public boolean hasSubordinates(Long employeeId) {
        return employeeRepository.hasSubordinates(employeeId);
    }
    
    /**
     * Find the appropriate approver for a given employee based on hierarchy
     * If the immediate manager is not available, find the next manager up the chain
     */
    public Employee findAppropriateSupervisor(Employee requestor, List<Long> unavailableManagerIds) {
        if (requestor.getManager() == null) {
            return null; // No manager to approve
        }
        
        // Create approval chain (managers up the hierarchy)
        List<Employee> approvalChain = requestor.getApprovalChain();
        
        // Find the first available manager in the chain
        for (Employee manager : approvalChain) {
            if (!unavailableManagerIds.contains(manager.getId())) {
                return manager;
            }
        }
        
        // If no available manager in the hierarchy, return null
        return null;
    }
    
    /**
     * Get the full organizational hierarchy as a nested structure
     */
    public List<Employee> getOrganizationalHierarchy() {
        List<Employee> topLevelManagers = getTopLevelManagers();
        return topLevelManagers;
    }
    public Employee findApproverForEmployee(Long employeeId) {
        Employee employee = getEmployeeById(employeeId).orElse(null);
        if (employee == null) {
            return null;
        }
        
        // Return the manager as the approver
        return employee.getManager();
    }
    public Employee findNextApproverInHierarchy(Long currentApproverId) {
        Employee currentApprover = getEmployeeById(currentApproverId).orElse(null);
        if (currentApprover == null || currentApprover.getManager() == null) {
            return null;
        }
        
        // Return the manager's manager
        return currentApprover.getManager();
    }
    /**
     * Get the approval path as a string for keeping track of escalations
     */
    public String getEscalationPathString(Employee requestor, Employee actualApprover) {
        StringBuilder path = new StringBuilder();
        Employee current = requestor.getManager();
        
        while (current != null && !current.getId().equals(actualApprover.getId())) {
            path.append(current.getName())
                .append(" (")
                .append(current.getPosition())
                .append(") -> ");
            current = current.getManager();
        }
        
        if (current != null) {
            path.append(current.getName())
                .append(" (")
                .append(current.getPosition())
                .append(")");
        }
        
        return path.toString();
    }
}