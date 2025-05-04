package com.bezkoder.springjwt.controllers.HRModuleControllers;


import com.bezkoder.springjwt.dtos.HRModuleDtos.LeaveDTO;
import com.bezkoder.springjwt.HRModuleServices.LeaveService;
import com.bezkoder.springjwt.dtos.HRModuleDtos.SentimentDashboardDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.Leave;
import com.bezkoder.springjwt.models.HRModuleEntities.LeaveType;
import com.bezkoder.springjwt.repository.HRModuleRepository.LeaveRepository;
import com.bezkoder.springjwt.security.services.UserDetailsImpl;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.bezkoder.springjwt.payload.response.LeaveCreationResponse;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;
    private LeaveRepository leaveRepository;
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated or authentication details are unavailable.");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    // --- MODIFIED POST (Ensure Employee ID is set correctly) ---
    @PostMapping
    @PreAuthorize("isAuthenticated()") // Any authenticated user can request leave
    public ResponseEntity<?> addLeave(@RequestBody LeaveDTO leaveDTO) {
        try {
            Long currentUserId = getCurrentUserId();

            // Vérifie que l'utilisateur demande un congé pour lui-même
            if (leaveDTO.getEmployeeId() == null) {
                leaveDTO.setEmployeeId(currentUserId);
            } else if (!leaveDTO.getEmployeeId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only create leave requests for yourself.");
            }

            // Création du congé
            LeaveDTO createdLeave = leaveService.addLeave(leaveDTO);

            // Message de prédiction
            String message = (leaveDTO.getLeaveType() != null)
                    ? "Predicted leave type: " + leaveDTO.getLeaveType()
                    : "No prediction was made.";

            LeaveCreationResponse response = new LeaveCreationResponse(createdLeave, message);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (AuthenticationCredentialsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error adding leave: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal error occurred while adding leave.");
        }
    }

    @GetMapping("/dashboard/sentiments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')") // Ajuster les rôles
    public ResponseEntity<?> getLeaveSentimentDashboardData() {
        try {
            SentimentDashboardDTO dashboardData = leaveService.getSentimentDashboardData();
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            // Loguer l'erreur e
            System.err.println("Error fetching sentiment dashboard data: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal error occurred while fetching dashboard data.");
        }
    }
    // --- GET All (Keep as is - for Admin/HR) ---
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<LeaveDTO>> getAllLeaves() {
        // ... (keep existing implementation using leaveService.getAllLeaves()) ...
        try {
            List<LeaveDTO> leaves = leaveService.getAllLeaves();
            return new ResponseEntity<>(leaves, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error getting all leaves: " + e.getMessage()); e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- MODIFIED GET by ID (Pass current user ID for Auth Check in Service) ---
    @GetMapping("/{leaveId}")
    @PreAuthorize("isAuthenticated()") // Check authorization in service layer
    public ResponseEntity<?> getLeaveById(@PathVariable Long leaveId) {
        try {
            Long currentUserId = getCurrentUserId();
            // Service layer will check if currentUserId owns the leave or is Admin/HR
            LeaveDTO leave = leaveService.getLeaveById(leaveId, currentUserId); // Pass current user ID
            return new ResponseEntity<>(leave, HttpStatus.OK);
        } catch (AuthenticationCredentialsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error getting leave by ID " + leaveId + ": " + e.getMessage()); e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }


    // --- NEW Endpoint: Get Leaves By Employee ID ---
    /**
     * GET: Retrieve all leave requests for a specific employee.
     * Accessible only by the employee themselves or by Admin/HR.
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("isAuthenticated()") // Check specific authorization below
    public ResponseEntity<?> getLeavesByEmployeeId(@PathVariable Long employeeId) {
        try {
            Long currentUserId = getCurrentUserId();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdminOrHr = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN") ||
                            grantedAuthority.getAuthority().equals("ROLE_HR"));

            // Authorization Check: Allow if the requested ID matches the logged-in user OR if the user is Admin/HR
            if (!currentUserId.equals(employeeId) && !isAdminOrHr) {
                // Log attempt for security audit?
                System.out.println("Authorization failed: User " + currentUserId + " tried to access leaves for employee " + employeeId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to view these leave requests.");
            }

            // Delegate to service, no need for user ID here as auth is done
            List<LeaveDTO> leaves = leaveService.getLeavesByEmployeeId(employeeId);
            return ResponseEntity.ok(leaves);
        } catch (AuthenticationCredentialsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            // This might mean the employee doesn't exist
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found or has no leave requests.");
        } catch (Exception e) {
            System.err.println("Error getting leaves for employee " + employeeId + ": " + e.getMessage()); e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    // --- NEW Endpoint: Get Leave Types ---
    /**
     * GET: Retrieve available leave types.
     */
    @GetMapping("/types")
    @PreAuthorize("isAuthenticated()") // All authenticated users can see types
    public ResponseEntity<List<String>> getLeaveTypes() {
        try {
            // Get enum names as strings
            List<String> types = Arrays.stream(LeaveType.values())
                    .map(Enum::name) // Returns enum constant name (e.g., "ANNUAL")
                    .collect(Collectors.toList());
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            System.err.println("Error getting leave types: " + e.getMessage()); e.printStackTrace();
            // Return an empty list or error status
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }


    // --- MODIFIED PUT (Pass current user ID for Auth Check in Service) ---
    /**
     * PUT: Update an existing leave request.
     * Only the employee who owns the request can update it, and only if it's PENDING.
     */
    @PutMapping("/{leaveId}")
    @PreAuthorize("isAuthenticated()") // Check ownership in service
    public ResponseEntity<?> updateLeave(@PathVariable Long leaveId, @RequestBody LeaveDTO leaveDTO) {
        try {
            // Basic ID consistency check
            if (leaveDTO.getLeaveId() != null && !leaveDTO.getLeaveId().equals(leaveId)) {
                return ResponseEntity.badRequest().body("Leave ID in path does not match ID in request body.");
            }
            // Ensure the leaveId from path is used if body ID is null
            if(leaveDTO.getLeaveId() == null) {
                leaveDTO.setLeaveId(leaveId);
            }

            Long currentUserId = getCurrentUserId();
            // Service layer handles authorization (ownership & PENDING status)
            LeaveDTO updatedLeave = leaveService.updateLeave(leaveId, leaveDTO, currentUserId);
            return new ResponseEntity<>(updatedLeave, HttpStatus.OK);

        } catch (AuthenticationCredentialsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) { // Catch validation/state errors
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error updating leave " + leaveId + ": " + e.getMessage()); e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    // --- MODIFIED DELETE (Pass current user ID for Auth Check in Service) ---
    /**
     * DELETE: Delete a leave request.
     * Only the employee who owns the request can delete it, and only if it's PENDING.
     */
    @DeleteMapping("/{leaveId}")
    @PreAuthorize("isAuthenticated()") // Check ownership in service
    public ResponseEntity<?> deleteLeave(@PathVariable Long leaveId) {
        try {
            Long currentUserId = getCurrentUserId();
            // Service layer handles authorization (ownership & PENDING status)
            leaveService.deleteLeave(leaveId, currentUserId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Success, no content

        } catch (AuthenticationCredentialsNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) { // Catch state errors (e.g., not PENDING)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error deleting leave " + leaveId + ": " + e.getMessage()); e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    // --- Admin/HR Actions (Approve/Reject) - Keep As Is or ensure service call is correct ---
    @PutMapping("/{leaveId}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> approveLeave(@PathVariable Long leaveId) {
        // ... (existing implementation should be fine if it calls service correctly) ...
        try {
            Long actionUserId = getCurrentUserId();
            LeaveDTO approvedLeave = leaveService.approveLeave(leaveId, actionUserId);
            return new ResponseEntity<>(approvedLeave, HttpStatus.OK);
        } catch (AuthenticationCredentialsNotFoundException e) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage()); }
        catch (EntityNotFoundException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
        catch (IllegalStateException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
        catch (Exception e) { System.err.println("Error approving leave: " + e.getMessage()); e.printStackTrace(); return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred."); }
    }

    @PutMapping("/{leaveId}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> rejectLeave(@PathVariable Long leaveId) {
        // ... (existing implementation should be fine if it calls service correctly) ...
        try {
            Long actionUserId = getCurrentUserId();
            LeaveDTO rejectedLeave = leaveService.rejectLeave(leaveId, actionUserId);
            return new ResponseEntity<>(rejectedLeave, HttpStatus.OK);
        } catch (AuthenticationCredentialsNotFoundException e) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage()); }
        catch (EntityNotFoundException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
        catch (IllegalStateException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
        catch (Exception e) { System.err.println("Error rejecting leave: " + e.getMessage()); e.printStackTrace(); return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred."); }
    }


}

