package com.example.approvalhierarchy.repository;

import com.example.approvalhierarchy.model.Employee;
import com.example.approvalhierarchy.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    
    List<Request> findByRequestorIdOrderByCreatedAtDesc(Long requestorId);
    
    List<Request> findByApproverIdOrderByCreatedAtDesc(Long approverId);
    Optional<Request> getRequestById(Long id);

    
    List<Request> findByStatusOrderByCreatedAtDesc(Request.RequestStatus status);
    long countByStatus(Request.RequestStatus status);
    long countByApproverIdAndStatus(Long approverId, Request.RequestStatus status);

    
    @Query("SELECT r FROM Request r WHERE r.approver.id = :approverId AND r.status = 'PENDING' ORDER BY r.createdAt DESC")
    List<Request> findPendingRequestsByApproverId(Long approverId);
    
    @Query("SELECT r FROM Request r WHERE r.requestor.id = :requestorId AND r.status = :status ORDER BY r.createdAt DESC")
    List<Request> findRequestsByRequestorAndStatus(Long requestorId, Request.RequestStatus status);
    
    // Find all requests regardless of status
    List<Request> findAllByOrderByCreatedAtDesc();
}