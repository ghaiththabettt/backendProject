package com.bezkoder.springjwt.HRModuleServices;

import com.bezkoder.springjwt.dtos.HRModuleDtos.TaskDTO;
import com.bezkoder.springjwt.models.Employee; // Import nécessaire
import com.bezkoder.springjwt.models.HRModuleEntities.Task;
import com.bezkoder.springjwt.models.HRModuleEntities.TaskStatus;
import com.bezkoder.springjwt.repository.HRModuleRepository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate; // Import nécessaire

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Transactional
    public TaskDTO completeTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        // Vérifier si elle n'est pas déjà complétée
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Task is already completed.");
        }

        // Mettre à jour statut ET date de complétion via la méthode de l'entité
        task.markAsCompleted();

        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask); // Appel de la méthode implémentée
    }

    // Méthode convertToDTO (Implémentée)
    private TaskDTO convertToDTO(Task task) {
        if (task == null) {
            return null;
        }

        TaskDTO dto = new TaskDTO();
        dto.setTaskId(task.getTaskId());
        dto.setTaskName(task.getTaskName());
        dto.setDescription(task.getDescription());
        // Convertir l'enum en String pour le DTO, gérer le cas null
        dto.setStatus(task.getStatus() != null ? task.getStatus().name() : null);
        dto.setCompletionDate(task.getCompletionDate());

        Employee employee = task.getEmployee(); // Récupérer l'employé lié
        if (employee != null) {
            dto.setEmployeeId(employee.getId());
            // Construire le nom complet, gérer les noms/prénoms nulls
            String firstName = employee.getName() != null ? employee.getName() : "";
            String lastName = employee.getLastName() != null ? employee.getLastName() : "";
            dto.setEmployeeFullName((firstName + " " + lastName).trim()); // trim() enlève les espaces superflus si l'un est null
        } else {
            // Si aucune employé n'est assigné à la tâche
            dto.setEmployeeId(null);
            dto.setEmployeeFullName("Non assigné"); // Ou retourner null, selon le besoin
        }

        return dto;
    }

    // Ajoutez ici d'autres méthodes de service pour les tâches si nécessaire
    // (createTask, getTaskById, getTasksByEmployee, updateTaskDetails, deleteTask, etc.)
}