package com.example.approvalhierarchy.service;

import com.example.approvalhierarchy.model.Employee;
import com.example.approvalhierarchy.model.Request;
import com.example.approvalhierarchy.model.Request.RequestStatus;
import com.example.approvalhierarchy.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RequestService {
    
    private final RequestRepository requestRepository;
    private final EmployeeService employeeService;
    
    @Autowired
    public RequestService(RequestRepository requestRepository, EmployeeService employeeService) {
        this.requestRepository = requestRepository;
        this.employeeService = employeeService;
    }
    
    public List<Request> getAllRequests() {
        return requestRepository.findAllByOrderByCreatedAtDesc();
    }
    
    public Request getRequestById(Long id) {
        Request request = requestRepository.findById(id)
        	.orElseThrow(() -> new RuntimeException("Request not found"));
        
        if (request.getStatus() == null) {
            request.setStatus(RequestStatus.PENDING); // or some default value
        }
        
        return request;
    }

    @Transactional
    public Request createRequest(Request request, List<Long> unavailableManagerIds) {
        Employee requestor = request.getRequestor();
        
        // Find the appropriate approver based on hierarchy and availability
        Employee approver = employeeService.findAppropriateSupervisor(requestor, unavailableManagerIds);
        
        if (approver == null) {
            // Handle case where no approver is available
            request.setStatus(Request.RequestStatus.CANCELLED);
            request.setComments("No approvers available in the hierarchy chain.");
            return requestRepository.save(request);
        }
        
        // Check if the request was escalated (approver is not the immediate manager)
        boolean escalated = !approver.getId().equals(requestor.getManager().getId());
        request.setEscalated(escalated);
        
        // If escalated, record the escalation path
        if (escalated) {
            String escalationPath = employeeService.getEscalationPathString(requestor, approver);
            request.setEscalationPath(escalationPath);
        }
        
        request.setApprover(approver);
        request.setCurrentApprover(approver);  // Set the current approver to be the same as approver initially
        request.setStatus(Request.RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        
        return requestRepository.save(request);
    }
    
    @Transactional
    public Request updateRequest(Request request) {
        return requestRepository.save(request);
    }
    
    @Transactional
    public Request approveRequest(Long requestId) {
        Request request = getRequestById(requestId);
        if (request == null) {
            return null;
        }
        
        // Set the status to APPROVED - this line is crucial
        request.setStatus(Request.RequestStatus.APPROVED);
        
        // Other logic related to approvers
        if (request.getCurrentApprover() == null) {
            // Handle null approver case
        } else if (request.getCurrentApprover().getManager() == null) {
            // At highest level
            request.setCurrentApprover(null);
        } else {
            // Move to next level in hierarchy if needed
            Employee nextApprover = employeeService.findNextApproverInHierarchy(request.getCurrentApprover().getId());
            request.setCurrentApprover(nextApprover);
        }
        
        // Save and return the updated request
        return requestRepository.save(request);
    }
    @Transactional
    public Request rejectRequest(Long requestId, String comments) {
        Optional<Request> requestOpt = requestRepository.findById(requestId);
        if (requestOpt.isPresent()) {
            Request request = requestOpt.get();
            request.setStatus(Request.RequestStatus.REJECTED);
            request.setComments(comments);
            request.setUpdatedAt(LocalDateTime.now());
            return requestRepository.save(request);
        }
        return null;
    }
    
    @Transactional
    public Request cancelRequest(Long requestId) {
        Optional<Request> requestOpt = requestRepository.findById(requestId);
        if (requestOpt.isPresent()) {
            Request request = requestOpt.get();
            request.setStatus(Request.RequestStatus.CANCELLED);
            request.setUpdatedAt(LocalDateTime.now());
            return requestRepository.save(request);
        }
        return null;
    }
    
    @Transactional
    public Request reassignRequest(Long requestId, Long newApproverId, boolean dueTounavailability) {
        Optional<Request> requestOpt = requestRepository.findById(requestId);
        Optional<Employee> approverOpt = employeeService.getEmployeeById(newApproverId);
        
        if (requestOpt.isPresent() && approverOpt.isPresent()) {
            Request request = requestOpt.get();
            Employee oldApprover = request.getApprover();
            Employee newApprover = approverOpt.get();
            
            request.setApprover(newApprover);
            
            // Mark as escalated if due to unavailability
            if (dueTounavailability) {
                request.setEscalated(true);
                
                // Update escalation path
                String escalationInfo = (request.getEscalationPath() != null && !request.getEscalationPath().isEmpty())
                    ? request.getEscalationPath() + " -> "
                    : "";
                
                escalationInfo += oldApprover.getName() + " (unavailable) -> " + newApprover.getName();
                request.setEscalationPath(escalationInfo);
            }
            
            request.setUpdatedAt(LocalDateTime.now());
            return requestRepository.save(request);
        }
        return null;
    }
    @Transactional
    public void updateMissingCurrentApprovers() {
        List<Request> pendingRequests = requestRepository.findByStatusOrderByCreatedAtDesc(Request.RequestStatus.PENDING);
        
        for (Request request : pendingRequests) {
            if (request.getCurrentApprover() == null && request.getApprover() != null) {
                request.setCurrentApprover(request.getApprover());
                requestRepository.save(request);
            }
        }
    }
    public long getPendingRequestCount() {
        return requestRepository.countByStatus(Request.RequestStatus.PENDING);
    }

    public long getPendingRequestsForApproverCount(Long approverId) {
        return requestRepository.countByApproverIdAndStatus(approverId, Request.RequestStatus.PENDING);
    }

    public List<Request> getRequestsByRequestor(Long requestorId) {
        return requestRepository.findByRequestorIdOrderByCreatedAtDesc(requestorId);
    }
    
    public List<Request> getRequestsByApprover(Long approverId) {
        return requestRepository.findByApproverIdOrderByCreatedAtDesc(approverId);
    }
    
    public List<Request> getPendingRequestsByApprover(Long approverId) {
        return requestRepository.findPendingRequestsByApproverId(approverId);
    }
    
    public List<Request> getRequestsByStatus(Request.RequestStatus status) {
        return requestRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    public List<Request> getRequestsByRequestorAndStatus(Long requestorId, Request.RequestStatus status) {
        return requestRepository.findRequestsByRequestorAndStatus(requestorId, status);
    }
}