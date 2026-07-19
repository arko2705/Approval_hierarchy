package com.example.approvalhierarchy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Title is required. This is a manually configured error.")
	private String title;

	@Column(columnDefinition = "TEXT") // greater than varchar(255)
	private String description;

	@ManyToOne(fetch = FetchType.LAZY) // check last line for full clarity
	@JoinColumn(name = "requestor_id") // -->FK that points to the Employee who created the request.
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Employee requestor;// saare employees under requestor id will be referred as requestors

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "approver_id")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Employee approver;// desc- FK of the Employee who FINALLY approved/rejected request (set ONCE the
								// workflow finishes-after the request is finally marked APPROVED or REJECTED.).

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "current_approver_id")
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Employee currentApprover;// FK pointing to the Employee currently responsible for acting on request (used
										// while it’s moving up the hierarchy). Once request finishes,
										// current_approver_id=null or =approver_id.

	@Enumerated(EnumType.STRING)
	private RequestStatus status = RequestStatus.PENDING;// Current workflow state – PENDING, APPROVED, REJECTED,
															// CANCELLED.

	private LocalDateTime createdAt = LocalDateTime.now();

	private LocalDateTime updatedAt;

	@Column(columnDefinition = "TEXT")
	private String comments;// optional text for both approver and requestor

	@Column(nullable = false)
	private Boolean escalated = false;// Stored 0(false)/1(true) in MySQL; standard boolean representation. true = the
										// request skipped an unavailable manager and moved up the chain.

	// List of managers who were unavailable before the request reached the current
	// approver
	@Column(columnDefinition = "TEXT")
	private String escalationPath;// Textual list (comma‑separated) of unavailable manager IDs before
									// request reached its current approver.

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public enum RequestStatus {
		PENDING, APPROVED, REJECTED, CANCELLED
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Employee getRequestor() {
		return requestor;
	}

	public void setRequestor(Employee requestor) {
		this.requestor = requestor;
	}

	public Employee getApprover() {
		return approver;
	}

	public void setApprover(Employee approver) {
		this.approver = approver;
	}

	public RequestStatus getStatus() {
		return status;
	}

	public void setStatus(RequestStatus status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public boolean isEscalated() {
		return escalated;
	}

	public void setEscalated(boolean escalated) {
		this.escalated = escalated;
	}

	public String getEscalationPath() {
		return escalationPath;
	}

	public void setEscalationPath(String escalationPath) {
		this.escalationPath = escalationPath;
	}

	public Employee getCurrentApprover() {
		return currentApprover;
	}

	public void setCurrentApprover(Employee currentApprover) {
		this.currentApprover = currentApprover;
	}

}
// so like fetch lazy is used here for a foreign key type shi column, in which
// this column(requestor_id) holds references to the employee table. Now
// everytime some row of request fetched,request_id will also be fetched,but
// request_id holds entire employee details in each column(or atleast points to
// it),so will the entire thing be loaded?
// No when fetch type lazy. Only the pointers will be loaded. Only when
// getRequestor() or other such sub getters/setters used,will the data be
// actually loaded from the target tabe

// is my concept spot on. YES OR NO

// FAQ:1.btw cant many requests have many approvers?
// so its escalation,so at a time one request shld be handled by one person only