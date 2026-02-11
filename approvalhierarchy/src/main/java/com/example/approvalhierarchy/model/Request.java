package com.example.approvalhierarchy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "Requestor is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestor_id")
    private Employee requestor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private Employee approver;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_approver_id")
    private Employee currentApprover;
    private RequestStatus status1;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    @Column(columnDefinition = "TEXT")
    private String comments;
    
    // When escalation happened because the immediate manager was unavailable
    @Column(nullable = false)
    private Boolean escalated = false;
    
    // List of managers who were unavailable before the request reached the current approver
    @Column(columnDefinition = "TEXT")
    private String escalationPath;
    
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
		return status1;
	}

	public void setStatus(RequestStatus status) {
		this.status1 = status;
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

    public RequestStatus getStatus1() {
       return status;
}

public void setStatus1(RequestStatus status) {
    this.status = status;
}
}
