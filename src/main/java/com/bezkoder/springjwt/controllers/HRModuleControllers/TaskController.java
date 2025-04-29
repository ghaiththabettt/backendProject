package com.bezkoder.springjwt.controllers.HRModuleControllers;

import com.bezkoder.springjwt.HRModuleServices.TaskService;
import com.bezkoder.springjwt.dtos.HRModuleDtos.TaskDTO;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import jakarta.persistence.EntityNotFoundException; // Import nécessaire
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Import nécessaire
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks") // Endpoint de base pour les tâches
@CrossOrigin(origins = "*", maxAge = 3600)
public class TaskController {

    @Autowired
    private TaskService taskService;

    // --- Endpoint pour marquer une tâche comme complétée ---
    @PutMapping("/{taskId}/complete")
    // Qui peut compléter une tâche ? L'employé assigné ? Un manager ? Un admin ?
    // Adaptez la règle @PreAuthorize selon vos besoins.
    // Mettre @PreAuthorize("isAuthenticated()") est un début.
    // Vous pourriez avoir besoin d'une logique plus complexe dans le service
    // pour vérifier si l'utilisateur courant a le droit de compléter CETTE tâche.
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markTaskAsComplete(@PathVariable Long taskId) {
        try {
            TaskDTO completedTask = taskService.completeTask(taskId);
            return ResponseEntity.ok(completedTask);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Error: Tâche non trouvée avec l'ID " + taskId)); // 404
        } catch (IllegalStateException e) {
            // Ex: Tâche déjà complétée
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Error: Impossible de compléter la tâche - " + e.getMessage())); // 400
        } catch (Exception e) {
            // Log l'erreur côté serveur
            System.err.println("Erreur lors de la complétion de la tâche " + taskId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error: Une erreur interne est survenue lors de la complétion de la tâche.")); // 500
        }
    }

    // --- AJOUTEZ ICI D'AUTRES ENDPOINTS POUR LES TÂCHES ---
    // - POST /api/tasks (pour créer une tâche, nécessite une méthode dans TaskService)
    // - GET /api/tasks/{taskId} (pour voir les détails d'une tâche)
    // - GET /api/tasks/employee/{employeeId} (pour voir les tâches d'un employé)
    // - PUT /api/tasks/{taskId} (pour modifier description, assignation, etc.)
    // - DELETE /api/tasks/{taskId} (pour supprimer une tâche)
    // ----------------------------------------------------
}