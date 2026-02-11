package com.example.approvalhierarchy.repository;

import com.example.approvalhierarchy.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    List<Employee> findByManagerId(Long managerId);
    
    List<Employee> findByManagerIsNull();
    
    Optional<Employee> findByEmail(String email);
    
    @Query("SELECT e FROM Employee e WHERE e.manager.id = :managerId")
    List<Employee> findAllSubordinates(Long managerId);
    
    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.manager.id = :employeeId")
    boolean hasSubordinates(Long employeeId);
}