package com.bezkoder.springjwt.controllers.HRModuleControllers;


import com.bezkoder.springjwt.dtos.HRModuleDtos.LeaveDTO;
import com.bezkoder.springjwt.HRModuleServices.LeaveService;
import com.bezkoder.springjwt.security.services.UserDetailsImpl;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Vérifie si l'authentification existe, est authentifiée et a un principal
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated or authentication details are unavailable.");
        }

        Object principal = authentication.getPrincipal();

        // Vérifie si le principal est une instance de votre UserDetailsImpl
        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            return userDetails.getId(); // Renvoie l'ID utilisateur depuis UserDetailsImpl
        } else {
            // Ce cas ne devrait idéalement pas se produire avec une authentification standard
            // Loggez le type pour le débogage.
            System.err.println("Unexpected principal type: " + principal.getClass().getName());
            throw new AuthenticationCredentialsNotFoundException("Cannot determine user ID from principal type: " + principal.getClass().getName());
        }
    }

    // POST: Add a new leave request
    // @PreAuthorize("isAuthenticated()") // Ou spécifiez les rôles
    @PostMapping
    public ResponseEntity<?> addLeave(@RequestBody LeaveDTO leaveDTO) {
        try {
            // Validation ou logique supplémentaire si nécessaire...
            if (leaveDTO.getEmployeeId() == null) {
                return ResponseEntity.badRequest().body("Employee ID must be provided in the request.");
            }
            LeaveDTO createdLeave = leaveService.addLeave(leaveDTO);
            return new ResponseEntity<>(createdLeave, HttpStatus.CREATED);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error adding leave: " + e.getMessage()); // Log l'erreur
            e.printStackTrace(); // Pour le débogage
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred while adding leave.");
        }
    }

    // GET: Retrieve all leave requests
    // @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<LeaveDTO>> getAllLeaves() {
        // Ajoutez une gestion d'erreurs si nécessaire
        try {
            List<LeaveDTO> leaves = leaveService.getAllLeaves();
            return new ResponseEntity<>(leaves, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error getting all leaves: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Ou une réponse d'erreur
        }
    }

    // GET: Retrieve a specific leave request by ID
    // @PreAuthorize("isAuthenticated()") // Ajoutez une logique d'autorisation plus fine ici
    @GetMapping("/{leaveId}")
    public ResponseEntity<?> getLeaveById(@PathVariable Long leaveId) {
        try {
            LeaveDTO leave = leaveService.getLeaveById(leaveId);
            // Ajoutez ici une vérification d'autorisation (l'utilisateur est le propriétaire ou HR/Admin)
            return new ResponseEntity<>(leave, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error getting leave by ID: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    // PUT: Update an existing leave request
    // @PreAuthorize("isAuthenticated()") // Ajoutez une logique d'autorisation plus fine ici
    @PutMapping("/{leaveId}")
    public ResponseEntity<?> updateLeave(@PathVariable Long leaveId, @RequestBody LeaveDTO leaveDTO) {
        try {
            if (leaveDTO.getLeaveId() != null && !leaveDTO.getLeaveId().equals(leaveId)) {
                return ResponseEntity.badRequest().body("Leave ID in path does not match ID in request body.");
            }
            leaveDTO.setLeaveId(leaveId);
            // Ajoutez ici des vérifications d'autorisation
            LeaveDTO updatedLeave = leaveService.updateLeave(leaveId, leaveDTO);
            return new ResponseEntity<>(updatedLeave, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error updating leave: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    // DELETE: Delete a leave request
    // @PreAuthorize("isAuthenticated()") // Ajoutez une logique d'autorisation plus fine ici
    @DeleteMapping("/{leaveId}")
    public ResponseEntity<?> deleteLeave(@PathVariable Long leaveId) {
        try {
            // Ajoutez ici des vérifications d'autorisation
            leaveService.deleteLeave(leaveId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error deleting leave: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    // PUT: Approve a leave request
    // @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    @PutMapping("/{leaveId}/approve")
    public ResponseEntity<?> approveLeave(@PathVariable Long leaveId) {
        try {
            Long actionUserId = getCurrentUserId(); // Obtient l'ID de l'utilisateur authentifié
            LeaveDTO approvedLeave = leaveService.approveLeave(leaveId, actionUserId);
            return new ResponseEntity<>(approvedLeave, HttpStatus.OK);
        } catch (AuthenticationCredentialsNotFoundException e) { // Gère l'erreur d'authentification
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // Leave ou User non trouvé
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // Ex: statut incorrect
        } catch (Exception e) {
            System.err.println("Error approving leave: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }

    // PUT: Reject a leave request
    // @PreAuthorize("hasRole('HR') or hasRole('ADMIN')")
    @PutMapping("/{leaveId}/reject")
    public ResponseEntity<?> rejectLeave(@PathVariable Long leaveId) {
        try {
            Long actionUserId = getCurrentUserId(); // Obtient l'ID de l'utilisateur authentifié
            LeaveDTO rejectedLeave = leaveService.rejectLeave(leaveId, actionUserId);
            return new ResponseEntity<>(rejectedLeave, HttpStatus.OK);
        } catch (AuthenticationCredentialsNotFoundException e) { // Gère l'erreur d'authentification
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // Leave ou User non trouvé
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // Ex: statut incorrect
        } catch (Exception e) {
            System.err.println("Error rejecting leave: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal error occurred.");
        }
    }
}

