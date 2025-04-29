package com.bezkoder.springjwt.repository.HRModuleRepository;

import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.HRModuleEntities.Task;
import com.bezkoder.springjwt.models.HRModuleEntities.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByEmployee(Employee employee);

    // Trouver les tâches par statut
    List<Task> findByStatus(TaskStatus status);

    // Compter les tâches complétées pour un employé dans une période donnée
    @Query("SELECT COUNT(t) FROM Task t WHERE t.employee = :employee AND t.status = :status AND t.completionDate BETWEEN :startDate AND :endDate")
    long countCompletedTasksForEmployeeInPeriod(
            @Param("employee") Employee employee,
            @Param("status") TaskStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Alternative: Compter par ID employé (utilisé dans PayrollService)
    @Query("SELECT COUNT(t) FROM Task t WHERE t.employee.id = :employeeId AND t.status = :status AND t.completionDate BETWEEN :startDate AND :endDate")
    long countCompletedTasksByEmployeeIdInPeriod(
            @Param("employeeId") Long employeeId,
            @Param("status") TaskStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Trouver les tâches complétées dans une période donnée (pourrait être utile)
    List<Task> findByStatusAndCompletionDateBetween(TaskStatus status, LocalDate startDate, LocalDate endDate);
}
