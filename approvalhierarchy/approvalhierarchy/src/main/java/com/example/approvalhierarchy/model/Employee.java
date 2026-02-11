package com.example.approvalhierarchy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;
    
    private String position;
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public Employee getManager() {
		return manager;
	}

	public void setManager(Employee manager) {
		this.manager = manager;
	}

	public List<Employee> getSubordinates() {
		return subordinates;
	}

	public void setSubordinates(List<Employee> subordinates) {
		this.subordinates = subordinates;
	}

	public List<Request> getRequests() {
		return requests;
	}

	public void setRequests(List<Request> requests) {
		this.requests = requests;
	}

	public List<Request> getRequestsToApprove() {
		return requestsToApprove;
	}

	public void setRequestsToApprove(List<Request> requestsToApprove) {
		this.requestsToApprove = requestsToApprove;
	}

	private String department;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;
    
    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL)
    private List<Employee> subordinates = new ArrayList<>();
    
    @OneToMany(mappedBy = "requestor", cascade = CascadeType.ALL)
    private List<Request> requests = new ArrayList<>();
    
    @OneToMany(mappedBy = "approver", cascade = CascadeType.ALL)
    private List<Request> requestsToApprove = new ArrayList<>();
    
    // Helper methods
    public boolean isManager() {
        return !subordinates.isEmpty();
    }
    
    public boolean hasManager() {
        return manager != null;
    }
    
    public List<Employee> getApprovalChain() {
        List<Employee> chain = new ArrayList<>();
        Employee current = this.manager;
        
        while (current != null) {
            chain.add(current);
            current = current.getManager();
        }
        
        return chain;
    }
}