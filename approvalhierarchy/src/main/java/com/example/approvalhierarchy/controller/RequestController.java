package com.example.approvalhierarchy.controller;

import com.example.approvalhierarchy.model.Employee;
import com.example.approvalhierarchy.model.Request;
import com.example.approvalhierarchy.service.EmployeeService;
import com.example.approvalhierarchy.service.RequestService;
import com.example.approvalhierarchy.service.UserService;
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
import java.security.Principal;

@Controller
@RequestMapping("/requests")
public class RequestController {

    private final RequestService requestService;
    private final EmployeeService employeeService;
    private final UserService userService;

    @Autowired
    public RequestController(RequestService requestService, EmployeeService employeeService, UserService userService) {
        this.requestService = requestService;
        this.employeeService = employeeService;
        this.userService = userService;
    }

    @GetMapping
    public String listAllRequests(Model model, Principal principal) {
        if (principal != null) {
            com.example.approvalhierarchy.model.User currentUser = userService.getUserByUsername(principal.getName()).orElse(null);
            if (currentUser != null && currentUser.getEmployee() != null) {
                model.addAttribute("currentEmployeeId", currentUser.getEmployee().getId());
            }
        }
        List<Request> requests = requestService.getAllRequests();
        model.addAttribute("requests", requests);
        return "request/list";
    }

    @GetMapping("/pending") // we dont expose any other status as not necessary,intionally done that way
    public String listPendingRequests(Model model, Principal principal) {
        if (principal != null) {
            com.example.approvalhierarchy.model.User currentUser = userService.getUserByUsername(principal.getName()).orElse(null);
            if (currentUser != null && currentUser.getEmployee() != null) {
                model.addAttribute("currentEmployeeId", currentUser.getEmployee().getId());
            }
        }
        List<Request> pendingRequests = requestService.getRequestsByStatus(Request.RequestStatus.PENDING);
        model.addAttribute("requests", pendingRequests);
        model.addAttribute("status", "Pending");
        return "request/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, Principal principal) {
        model.addAttribute("request", new Request());
        boolean hasManager = false;
        
        if (principal != null) {
            com.example.approvalhierarchy.model.User currentUser = userService.getUserByUsername(principal.getName()).orElse(null);
            if (currentUser != null && currentUser.getEmployee() != null && currentUser.getEmployee().getManager() != null) {
                hasManager = true;
            }
        }
        
        model.addAttribute("hasManager", hasManager);
        return "request/form";
    }

    @PostMapping("/create")
    public String createRequest(@Valid @ModelAttribute("request") Request request,
            BindingResult result,
            Principal principal,
            Model model,
            RedirectAttributes attributes) {
            
        boolean hasManager = false;
        com.example.approvalhierarchy.model.User currentUser = null;
        
        if (principal != null) {
            currentUser = userService.getUserByUsername(principal.getName()).orElse(null);
            if (currentUser != null && currentUser.getEmployee() != null && currentUser.getEmployee().getManager() != null) {
                hasManager = true;
            }
        }
        
        if (result.hasErrors()) {
            model.addAttribute("hasManager", hasManager);
            return "request/form";
        }

        if (currentUser != null && currentUser.getEmployee() != null) {
            request.setRequestor(currentUser.getEmployee());
        } else {
            attributes.addFlashAttribute("error", "Logged in user does not have an associated employee profile.");
            return "redirect:/requests";
        }

        Request savedRequest = requestService.createRequest(request);

        if (savedRequest.getStatus() == Request.RequestStatus.CANCELLED) {
            attributes.addFlashAttribute("warning",
                    "Request could not be processed: No manager assigned to you.");
        } else {
            attributes.addFlashAttribute("success", "Request created successfully!");
        }

        return "redirect:/requests";
    }

    @GetMapping("/view/{id}")
    public String viewRequest(@PathVariable Long id, Model model, Principal principal, RedirectAttributes attributes) {
        try {
            Request request = requestService.getRequestById(id);
            if (principal != null) {
                com.example.approvalhierarchy.model.User currentUser = userService.getUserByUsername(principal.getName()).orElse(null);
                if (currentUser != null && currentUser.getEmployee() != null) {
                    model.addAttribute("currentEmployeeId", currentUser.getEmployee().getId());
                }
            }
            model.addAttribute("request", request);
            return "request/view";
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error", "Request not found!");
            return "redirect:/requests";
        }
    }

    @GetMapping("/approve/{id}")
    public String showApproveForm(@PathVariable Long id, Model model, Principal principal, RedirectAttributes attributes) {
        try {
            Request request = requestService.getRequestById(id);
            if (principal != null) {
                com.example.approvalhierarchy.model.User currentUser = userService.getUserByUsername(principal.getName()).orElse(null);
                if (currentUser != null && currentUser.getEmployee() != null && currentUser.getEmployee().getId().equals(request.getRequestor().getId())) {
                    attributes.addFlashAttribute("error", "You cannot approve your own request!");
                    return "redirect:/requests";
                }
            }
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
    public String approveRequest(@PathVariable Long id, Principal principal, RedirectAttributes attributes) {
        try {
            Request req = requestService.getRequestById(id);
            if (principal != null) {
                com.example.approvalhierarchy.model.User currentUser = userService.getUserByUsername(principal.getName()).orElse(null);
                if (currentUser != null && currentUser.getEmployee() != null && currentUser.getEmployee().getId().equals(req.getRequestor().getId())) {
                    attributes.addFlashAttribute("error", "You cannot approve your own request!");
                    return "redirect:/requests";
                }
            }
            
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
    public String showRejectForm(@PathVariable Long id, Model model, Principal principal, RedirectAttributes attributes) {
        try {
            Request request = requestService.getRequestById(id);
            if (principal != null) {
                com.example.approvalhierarchy.model.User currentUser = userService.getUserByUsername(principal.getName()).orElse(null);
                if (currentUser != null && currentUser.getEmployee() != null && currentUser.getEmployee().getId().equals(request.getRequestor().getId())) {
                    attributes.addFlashAttribute("error", "You cannot reject your own request!");
                    return "redirect:/requests";
                }
            }
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
            Principal principal,
            RedirectAttributes attributes) {
            
        try {
            Request req = requestService.getRequestById(id);
            if (principal != null) {
                com.example.approvalhierarchy.model.User currentUser = userService.getUserByUsername(principal.getName()).orElse(null);
                if (currentUser != null && currentUser.getEmployee() != null && currentUser.getEmployee().getId().equals(req.getRequestor().getId())) {
                    attributes.addFlashAttribute("error", "You cannot reject your own request!");
                    return "redirect:/requests";
                }
            }
        } catch (RuntimeException e) {
            // let service handle if not found
        }
            
        Request rejectedRequest = requestService.rejectRequest(id, comments);

        if (rejectedRequest != null) {
            attributes.addFlashAttribute("success", "Request rejected successfully!");
        } else {
            attributes.addFlashAttribute("error", "Failed to reject request!");
        }

        return "redirect:/requests";
    }

    @GetMapping("/cancel/{id}")
    public String cancelRequest(@PathVariable Long id, Principal principal, RedirectAttributes attributes) {
        try {
            Request req = requestService.getRequestById(id);
            if (principal != null) {
                com.example.approvalhierarchy.model.User currentUser = userService.getUserByUsername(principal.getName()).orElse(null);
                if (currentUser != null && currentUser.getEmployee() != null && !currentUser.getEmployee().getId().equals(req.getRequestor().getId())) {
                    attributes.addFlashAttribute("error", "You can only cancel your own requests!");
                    return "redirect:/requests";
                }
            }
            if (req.getStatus() != Request.RequestStatus.PENDING) {
                attributes.addFlashAttribute("error", "Only pending requests can be cancelled!");
                return "redirect:/requests";
            }
        } catch (RuntimeException e) {
            // let service handle if not found
        }
        
        Request cancelledRequest = requestService.cancelRequest(id);

        if (cancelledRequest != null) {
            attributes.addFlashAttribute("success", "Request cancelled successfully!");
        } else {
            attributes.addFlashAttribute("error", "Failed to cancel request!");
        }

        return "redirect:/requests";
    }

    @GetMapping("/reassign/{id}")
    public String showReassignForm(@PathVariable Long id, Model model, Principal principal, RedirectAttributes attributes) {
        try {
            Request request = requestService.getRequestById(id);
            if (principal != null) {
                com.example.approvalhierarchy.model.User currentUser = userService.getUserByUsername(principal.getName()).orElse(null);
                if (currentUser != null && currentUser.getEmployee() != null && currentUser.getEmployee().getId().equals(request.getRequestor().getId())) {
                    attributes.addFlashAttribute("error", "You cannot reassign your own request!");
                    return "redirect:/requests";
                }
            }
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
            Principal principal,
            RedirectAttributes attributes) {
            
        try {
            Request req = requestService.getRequestById(id);
            if (principal != null) {
                com.example.approvalhierarchy.model.User currentUser = userService.getUserByUsername(principal.getName()).orElse(null);
                if (currentUser != null && currentUser.getEmployee() != null && currentUser.getEmployee().getId().equals(req.getRequestor().getId())) {
                    attributes.addFlashAttribute("error", "You cannot reassign your own request!");
                    return "redirect:/requests";
                }
            }
        } catch (RuntimeException e) {
            // let service handle if not found
        }

        Request reassignedRequest = requestService.reassignRequest(id, newApproverId, false);

        if (reassignedRequest != null) {
            attributes.addFlashAttribute("success", "Request reassigned successfully!");
        } else {
            attributes.addFlashAttribute("error", "Failed to reassign request!");
        }

        return "redirect:/requests";
    }

    @GetMapping("/escalate/{id}")
    public String showEscalateForm(@PathVariable Long id, Model model, Principal principal, RedirectAttributes attributes) {
        try {
            Request request = requestService.getRequestById(id);
            if (principal != null) {
                com.example.approvalhierarchy.model.User currentUser = userService.getUserByUsername(principal.getName()).orElse(null);
                if (currentUser != null && currentUser.getEmployee() != null && currentUser.getEmployee().getId().equals(request.getRequestor().getId())) {
                    attributes.addFlashAttribute("error", "You cannot escalate your own request!");
                    return "redirect:/requests";
                }
            }
            if (request.getStatus() == Request.RequestStatus.PENDING) {
                if (request.getApprover() == null || request.getApprover().getManager() == null) {
                    attributes.addFlashAttribute("error", "No immediate supervisor found to escalate to!");
                    return "redirect:/requests/view/" + id;
                }
                model.addAttribute("request", request);
                model.addAttribute("supervisor", request.getApprover().getManager());
                return "request/escalate";
            } else {
                attributes.addFlashAttribute("error", "Request not found or already processed!");
                return "redirect:/requests";
            }
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error", "Request not found or already processed!");
            return "redirect:/requests";
        }
    }

    @PostMapping("/escalate/{id}")
    public String escalateRequest(@PathVariable Long id, Principal principal, RedirectAttributes attributes) {
        try {
            Request req = requestService.getRequestById(id);
            if (principal != null) {
                com.example.approvalhierarchy.model.User currentUser = userService.getUserByUsername(principal.getName()).orElse(null);
                if (currentUser != null && currentUser.getEmployee() != null && currentUser.getEmployee().getId().equals(req.getRequestor().getId())) {
                    attributes.addFlashAttribute("error", "You cannot escalate your own request!");
                    return "redirect:/requests";
                }
            }
            if (req.getApprover() == null || req.getApprover().getManager() == null) {
                attributes.addFlashAttribute("error", "No immediate supervisor found to escalate to!");
                return "redirect:/requests/view/" + id;
            }
            
            Request escalatedRequest = requestService.reassignRequest(id, req.getApprover().getManager().getId(), true);
            if (escalatedRequest != null) {
                attributes.addFlashAttribute("success", "Request escalated successfully!");
            } else {
                attributes.addFlashAttribute("error", "Failed to escalate request!");
            }
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("error", "Request not found!");
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

    // @GetMapping("/my-pending-approvals/{employeeId}")
    // public String getPendingApprovals(@PathVariable Long employeeId, Model model)
    // {
    // Optional<Employee> employeeOpt = employeeService.getEmployeeById(employeeId);

    // if (employeeOpt.isPresent()) {
    // Employee employee = employeeOpt.get();
    // List<Request> pendingApprovals =
    // requestService.getPendingRequestsByApprover(employeeId);

    // model.addAttribute("employee", employee);
    // model.addAttribute("pendingApprovals", pendingApprovals);

    // return "request/pending-approvals";
    // } else {
    // return "redirect:/employees";
    // }
    // }
    @PostMapping("/requests/fix-approvers")
    public String fixMissingApprovers(RedirectAttributes attributes) {
        requestService.updateMissingCurrentApprovers();
        attributes.addFlashAttribute("success", "Fixed missing current approvers for pending requests");
        return "redirect:/requests";
    }
}
