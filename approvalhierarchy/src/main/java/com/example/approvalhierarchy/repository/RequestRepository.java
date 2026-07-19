package com.example.approvalhierarchy.repository;

import com.example.approvalhierarchy.model.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequestorIdOrderByCreatedAtDesc(Long requestorId);// All requests created by a given user,
                                                                          // newest first.getRequestsByRequestor(Long
                                                                          // requestorId).Used on the employee profile
                                                                          // page to show that employee’s submitted
                                                                          // requests.TEST

    List<Request> findByApproverIdOrderByCreatedAtDesc(Long approverId);// all requests waiting to be approved by a
                                                                        // manager(pending)

    Optional<Request> getRequestById(Long id);// A single request identified by its DB id (or throws if not
                                              // found).(TEST)

    List<Request> findByStatusOrderByCreatedAtDesc(Request.RequestStatus status);// All requests having a specific
                                                                                 // status (e.g., APPROVED, CANCELLED),
                                                                                 // newest first.(TEST)

    long countByStatus(Request.RequestStatus status);// How many requests exist with a given status.(TEST)

    long countByApproverIdAndStatus(Long approverId, Request.RequestStatus status);// How many pending (or other‑status)
                                                                                   // requests a particular approver
                                                                                   // currently has.

    @Query("SELECT r FROM Request r WHERE r.approver.id = :approverId AND r.status = 'PENDING' ORDER BY r.createdAt ASC")
    List<Request> findPendingRequestsByApproverId(Long approverId);// All PENDING requests for a given approver, oldest
                                                                   // first (FIFO).TEST

    @Query("SELECT r FROM Request r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
    List<Request> findAllPendingOrderByCreatedAtAsc();// All PENDING requests across the whole system, oldest first.

    @Query("SELECT r FROM Request r WHERE r.requestor.id = :requestorId AND r.status = :status ORDER BY r.createdAt DESC")
    List<Request> findRequestsByRequestorAndStatus(Long requestorId, Request.RequestStatus status);// All requests from
                                                                                                   // a user that have a
                                                                                                   // particular status,
                                                                                                   // newest
    // first.(TEST/FIND OUT WHERE USED)

    List<Request> findByStatusOrderByCreatedAtAsc(Request.RequestStatus status);

    List<Request> findAllByOrderByCreatedAtDesc();// All requests, newest first.getAllRequests(). Called under /requests
}