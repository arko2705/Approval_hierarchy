package com.example.approvalhierarchy.model;

import jakarta.persistence.*; //Jakarta.persistence is used to import the database
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import com.example.approvalhierarchy.model.User;
import java.util.ArrayList;
import java.util.List;

//This is the core of the "hierarchy." It holds personal info but, most importantly, it maps out who reports to who.
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Name is required") // If you fill by database the values,and don't fill UserName, then name is
											// required error is displayed in terminal
	private String name;

	@Email(message = "Valid email is required")
	@NotBlank(message = "Email is required")
	private String email;

	private String position;

	private String department;

	@ManyToOne(fetch = FetchType.LAZY) // current table to target table.so these relationships help store ids(basically
										// Primary keys) that help map relationships of one row to anoteher row of
										// current table to target table
	@JoinColumn(name = "manager_id") // actually creates a column
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Employee manager;// target table

	@OneToMany(mappedBy = "manager", cascade = CascadeType.ALL) // manager_id stores managers,so for each manager
																// subordinates are mapped. In db,manager_id points to
																// employee id.List actually gets filled during runtime
																// where methods related to it are called.
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<Employee> subordinates = new ArrayList<>();

	@OneToMany(mappedBy = "requestor", cascade = CascadeType.ALL) // Cascade tells JPA what to do with the related rows
																	// when you act on the parent entity.CascadeType.ALL
																	// = all operations (save, update, delete, etc.) are
																	// cascaded
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<Request> requests = new ArrayList<>();

	@OneToMany(mappedBy = "approver", cascade = CascadeType.ALL)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<Request> requestsToApprove = new ArrayList<>();// btw these lists onyl hold values of specific
																// managers/employees during specific use cases at a
																// time after which they are overwritten if used again
																// later,during the same runtime instance?
	@OneToOne(mappedBy = "employee", cascade = CascadeType.REMOVE)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private User userAccount;// this is so that when an employee is deleted, the corresponding user account
								// is also deleted.

	public Long getId() { // These values come directly from the table.their varaible name is same in
							// table and here.
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
		return subordinates; //
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

	// Helper methods
	public boolean isManager() {
		return !subordinates.isEmpty();
	}

	public boolean hasManager() {
		return manager != null;
	}

	public List<Employee> getApprovalChain() { /// Man so basically this is getting all the employees above a certain
												/// employee
		List<Employee> chain = new ArrayList<>();
		Employee current = this.getManager(); // this is the current object calling the method. getManager() returns a
												// manager.

		while (current != null) {
			chain.add(current);
			current = current.getManager();
		}

		return chain;
	}
}

// When you remove a column from entity,hibernate's ddl auto=update feature
// checks for all the columns in db table,and if it finds columns that no longer
// exist/columns in entity thhat dont exist in table,it runs commands to adjust
// these changes in the db