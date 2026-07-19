package com.example.approvalhierarchy.repository;

import com.example.approvalhierarchy.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

//JPA interface basically providing you methods to talk to db
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByManagerId(Long managerId);// these are methods that we create,where we gotta follow
                                                   // JpaRepository's naming convention.

    List<Employee> findByManagerIsNull();

    Optional<Employee> findByEmail(String email);

    @Query("SELECT e FROM Employee e WHERE e.manager.id = :managerId")
    List<Employee> findAllSubordinates(Long managerId);// does the same thing as findBymanagerId

    @Query("SELECT COUNT(e) > 0 FROM Employee e WHERE e.manager.id = :employeeId")
    boolean hasSubordinates(Long employeeId);// Returns a boolean that is true if the employee has any subordinate.
}