package com.example.approvalhierarchy.controller;

import com.example.approvalhierarchy.model.Employee;
import com.example.approvalhierarchy.model.Request;
import com.example.approvalhierarchy.service.EmployeeService;
import com.example.approvalhierarchy.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/requests")
public class RequestController {
    
    private final RequestService requestService;
    private final EmployeeService employeeService;
    
    @Autowired
    public RequestController(RequestService requestService, EmployeeService employeeService) {
        this.requestService = requestService;
        this.employeeService = employeeService;
    }
    
    @GetMapping
    public String listAllRequests(Model model) {
        List<Request> requests = requestService.getAllRequests();
        model.addAttribute("requests", requests);
        return "request/list";
    }
    
    @GetMapping("/pending")
    public String listPendingRequests(Model model) {
        List<Request> pendingRequests = requestService.getRequestsByStatus(Request.RequestStatus.PENDING);
        model.addAttribute("requests", pendingRequests);
        model.addAttribute("status", "Pending");
        return "request/list";
    }
    
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("request", new Request());
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("unavailableManagers", new ArrayList<Long>());
        return "request/form";
    }
    
    @PostMapping("/create")
    public String createRequest(@Valid @ModelAttribute("request") Request request,
                              BindingResult result,
                              @RequestParam(value = "unavailableManagers", required = false) List<Long> unavailableManagers,
                              Model model,
                              RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute("employees", employeeService.getAllEmployees());
            return "request/form";
        }
        
        // Handle null unavailableManagers
        if (unavailableManagers == null) {
            unavailableManagers = new ArrayList<>();
        }
        
        Request savedRequest = requestService.createRequest(request, unavailableManagers);
        
        if (savedRequest.getStatus() == Request.RequestStatus.CANCELLED) {
            attributes.addFlashAttribute("warning", "Request could not be processed: No available approvers in the hierarchy.");
        } else if (savedRequest.isEscalated()) {
            attributes.addFlashAttribute("info", 
                "Request was escalated due to manager unavailability. Current approver: " + 
                savedRequest.getApprover().getName());
        } else {
            attributes.addFlashAttribute("success", "Request created successfully!");
        }
        
        return "redirect:/requests";
    }
    
    @GetMapping("/view/{id}")
    public String viewRequest(@PathVariable Long id, Model model, RedirectAttributes attributes) {
        try {
            Request request = requestService.getRequestById(id);
            model.addAttribute("request", request);
            return "request/view";
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error", "Request not found!");
            return "redirect:/requests";
        }
    }
    @GetMapping("/approve/{id}")
    public String showApproveForm(@PathVariable Long id, Model model, RedirectAttributes attributes) {
        try {
            Request request = requestService.getRequestById(id);
            // Check if the request is in PENDING state
            if (request.getStatus() == Request.RequestStatus.PENDING) {
                model.addAttribute("request", request);
                return "request/approve";
            } else {
                attributes.addFlashAttribute("error", "Request not found or already processed!");
                return "redirect:/requests";
            }
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error", "Request not found or already processed!");
            return "redirect:/requests";
        }
    }
    
    @PostMapping("/approve/{id}")
    public String approveRequest(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            Request request = requestService.approveRequest(id);
            if (request != null) {
                // Debug info
                System.out.println("Request #" + id + " approved with status: " + request.getStatus());
                attributes.addFlashAttribute("success", "Request approved successfully");
            } else {
                attributes.addFlashAttribute("error", "Failed to approve request");
            }
            return "redirect:/requests";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/requests";
        }
    }
    
    @GetMapping("/reject/{id}")
    public String showRejectForm(@PathVariable Long id, Model model, RedirectAttributes attributes) {
        try {
            Request request = requestService.getRequestById(id);
            // Check if the request is in PENDING state
            if (request.getStatus() == Request.RequestStatus.PENDING) {
                model.addAttribute("request", request);
                return "request/reject";
            } else {
                attributes.addFlashAttribute("error", "Request not found or already processed!");
                return "redirect:/requests";
            }
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error", "Request not found or already processed!");
            return "redirect:/requests";
        }
    }
    
    @PostMapping("/reject/{id}")
    public String rejectRequest(@PathVariable Long id, 
                              @RequestParam String comments,
                              RedirectAttributes attributes) {
        Request rejectedRequest = requestService.rejectRequest(id, comments);
        
        if (rejectedRequest != null) {
            attributes.addFlashAttribute("success", "Request rejected successfully!");
        } else {
            attributes.addFlashAttribute("error", "Failed to reject request!");
        }
        
        return "redirect:/requests";
    }
    
    @GetMapping("/cancel/{id}")
    public String cancelRequest(@PathVariable Long id, RedirectAttributes attributes) {
        Request cancelledRequest = requestService.cancelRequest(id);
        
        if (cancelledRequest != null) {
            attributes.addFlashAttribute("success", "Request cancelled successfully!");
        } else {
            attributes.addFlashAttribute("error", "Failed to cancel request!");
        }
        
        return "redirect:/requests";
    }
    
    @GetMapping("/reassign/{id}")
    public String showReassignForm(@PathVariable Long id, Model model, RedirectAttributes attributes) {
        try {
            Request request = requestService.getRequestById(id);
            // Check if the request is in PENDING state
            if (request.getStatus() == Request.RequestStatus.PENDING) {
                model.addAttribute("request", request);
                model.addAttribute("employees", employeeService.getAllEmployees());
                return "request/reassign";
            } else {
                attributes.addFlashAttribute("error", "Request not found or already processed!");
                return "redirect:/requests";
            }
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error", "Request not found or already processed!");
            return "redirect:/requests";
        }
    }
    
    @PostMapping("/reassign/{id}")
    public String reassignRequest(@PathVariable Long id,
                                @RequestParam Long newApproverId,
                                @RequestParam(defaultValue = "false") boolean dueTounavailability,
                                RedirectAttributes attributes) {
        Request reassignedRequest = requestService.reassignRequest(id, newApproverId, dueTounavailability);
        
        if (reassignedRequest != null) {
            attributes.addFlashAttribute("success", "Request reassigned successfully!");
        } else {
            attributes.addFlashAttribute("error", "Failed to reassign request!");
        }
        
        return "redirect:/requests";
    }
    
    @GetMapping("/by-employee/{employeeId}")
    public String getRequestsByEmployee(@PathVariable Long employeeId, Model model) {
        Optional<Employee> employeeOpt = employeeService.getEmployeeById(employeeId);
        
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            List<Request> submittedRequests = requestService.getRequestsByRequestor(employeeId);
            List<Request> requestsToApprove = requestService.getRequestsByApprover(employeeId);
            
            model.addAttribute("employee", employee);
            model.addAttribute("submittedRequests", submittedRequests);
            model.addAttribute("requestsToApprove", requestsToApprove);
            
            return "request/by-employee";
        } else {
            return "redirect:/employees";
        }
    }
    
    @GetMapping("/my-pending-approvals/{employeeId}")
    public String getPendingApprovals(@PathVariable Long employeeId, Model model) {
        Optional<Employee> employeeOpt = employeeService.getEmployeeById(employeeId);
        
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            List<Request> pendingApprovals = requestService.getPendingRequestsByApprover(employeeId);
            
            model.addAttribute("employee", employee);
            model.addAttribute("pendingApprovals", pendingApprovals);
            
            return "request/pending-approvals";
        } else {
            return "redirect:/employees";
        }
    }
    @PostMapping("/requests/fix-approvers")
    public String fixMissingApprovers(RedirectAttributes attributes) {
        requestService.updateMissingCurrentApprovers();
        attributes.addFlashAttribute("success", "Fixed missing current approvers for pending requests");
        return "redirect:/requests";
    }
}
    