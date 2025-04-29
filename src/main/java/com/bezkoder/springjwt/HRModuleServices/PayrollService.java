package com.bezkoder.springjwt.HRModuleServices;

import com.bezkoder.springjwt.dtos.HRModuleDtos.PayrollDTO;
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.HRModuleEntities.Payroll;
import com.bezkoder.springjwt.models.HRModuleEntities.TaskStatus;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.PayrollRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    private static final Logger log = LoggerFactory.getLogger(PayrollService.class);

    private static final float TASK_COMPLETION_BONUS = 100.0f;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    // Récupérer tous les enregistrements
    public List<Payroll> getAllPayrolls() {
        return payrollRepository.findAll();
    }

    // Récupérer un enregistrement par son identifiant
    public Optional<Payroll> getPayrollById(Long payrollId) {
        return payrollRepository.findById(payrollId);
    }

    // --- Logique de Génération de Paie avec Bonus (Décommentée) ---
    @Transactional // Important pour assurer la cohérence des données
    public void generatePayrollsForPeriod(LocalDate periodStartDate, LocalDate periodEndDate) {
        log.info("Début de la génération des fiches de paie pour la période {} - {}", periodStartDate, periodEndDate);

        List<Employee> employees = employeeRepository.findAll();
        if (employees.isEmpty()) {
            log.warn("Aucun employé trouvé. Aucune fiche de paie générée.");
            return;
        }

        Employee employeeWithMostCompletedTasks = null;
        long maxCompletedTasks = -1; // Initialiser à -1 pour distinguer 0 tâche du cas initial

        // 1. Trouver l'employé avec le plus de tâches complétées DANS LA PÉRIODE
        log.info("Calcul du bonus pour tâches complétées...");
        for (Employee employee : employees) {
            // Utilise la méthode du repository qui compte les tâches COMPLETED avec une completionDate dans la période
            long completedTasksCount = taskRepository.countCompletedTasksByEmployeeIdInPeriod(
                    employee.getId(),
                    TaskStatus.COMPLETED, // Cherche spécifiquement les tâches complétées
                    periodStartDate,
                    periodEndDate
            );

            log.debug("Employé ID {}: {} tâches complétées dans la période.", employee.getId(), completedTasksCount);

            if (completedTasksCount > 0 && completedTasksCount > maxCompletedTasks) { // Modifié : On ne compare que si > 0
                maxCompletedTasks = completedTasksCount;
                employeeWithMostCompletedTasks = employee;
            }
            // Gérer les égalités: pour l'instant, le premier trouvé gagne.
        }

        if (employeeWithMostCompletedTasks != null) { // Simplifié : si on a trouvé quelqu'un, c'est qu'il a > 0 tâches
            log.info("Bonus attribué à l'employé ID {} ({} tâches complétées)",
                    employeeWithMostCompletedTasks.getId(), maxCompletedTasks);
        } else {
            log.info("Aucun employé n'a complété de tâches dans cette période ou aucun employé trouvé. Aucun bonus attribué.");
        }


        // 2. Générer la fiche de paie pour chaque employé
        log.info("Génération des fiches de paie individuelles...");
        for (Employee employee : employees) {
            Payroll payroll = new Payroll();
            payroll.setEmployee(employee);
            payroll.setPayDate(periodEndDate); // Ou une date spécifique de paiement
            // TODO: Ajouter periodStartDate et periodEndDate à l'entité Payroll si nécessaire

            // Salaire de base (simplifié, pourrait dépendre du contrat, heures, etc.)
            payroll.setBasicSalary(employee.getSalary() != null ? employee.getSalary().floatValue() : 0.0f);

            // Calcul des bonus
            float currentBonuses = 0.0f;
            // Ajouter d'autres logiques de bonus ici si nécessaire...

            // Ajouter le bonus pour tâches complétées
            // L'employé reçoit le bonus SI il est celui identifié ET si un max > 0 a été trouvé
            if (employee.equals(employeeWithMostCompletedTasks)) {
                currentBonuses += TASK_COMPLETION_BONUS;
                log.debug("Ajout du bonus de {} à l'employé ID {}", TASK_COMPLETION_BONUS, employee.getId());
            }
            payroll.setBonuses(currentBonuses);

            // Calcul des déductions (exemple simple)
            // TODO: Implémenter une logique de déduction réelle (taxes, etc.)
            float currentDeductions = payroll.getBasicSalary() * 0.1f; // Exemple: 10%
            payroll.setDeductions(currentDeductions);

            // Calcul du salaire total
            float totalSalary = payroll.getBasicSalary() + payroll.getBonuses() - payroll.getDeductions();
            payroll.setTotalSalary(totalSalary);

            // Sauvegarder la fiche de paie
            payrollRepository.save(payroll);
            log.debug("Fiche de paie sauvegardée pour l'employé ID {}", employee.getId());
        }

        log.info("Génération des fiches de paie terminée.");
    }
    // --- Fin de la logique de génération ---


    // Créer une nouvelle fiche de paie (Gestion Manuelle)
    public Payroll createPayroll(Payroll payroll) {
        // Recalculer au cas où
        payroll.setTotalSalary(payroll.getBasicSalary() + payroll.getBonuses() - payroll.getDeductions());
        // Assigner l'employé est nécessaire ici si non fait avant l'appel
        if (payroll.getEmployee() == null && payroll.getEmployee().getId() != null) {
            Employee employee = employeeRepository.findById(payroll.getEmployee().getId())
                    .orElseThrow(() -> new RuntimeException("Employee non trouvé pour la fiche de paie"));
            payroll.setEmployee(employee);
        }
        return payrollRepository.save(payroll);
    }

    // Mettre à jour une fiche de paie existante (Gestion Manuelle)
    public Payroll updatePayroll(Long payrollId, Payroll payrollDetails) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll non trouvé avec l'id " + payrollId));

        // Valider et mettre à jour l'employé si l'ID est fourni et différent
        if (payrollDetails.getEmployee() != null && payrollDetails.getEmployee().getId() != null &&
                !payrollDetails.getEmployee().getId().equals(payroll.getEmployee().getId())) {
            Employee newEmployee = employeeRepository.findById(payrollDetails.getEmployee().getId())
                    .orElseThrow(() -> new RuntimeException("Nouvel Employee non trouvé pour la fiche de paie"));
            payroll.setEmployee(newEmployee);
        }

        payroll.setBasicSalary(payrollDetails.getBasicSalary());
        payroll.setBonuses(payrollDetails.getBonuses());
        payroll.setDeductions(payrollDetails.getDeductions());
        // Recalculer le total
        payroll.setTotalSalary(payroll.getBasicSalary() + payroll.getBonuses() - payroll.getDeductions());
        payroll.setPayDate(payrollDetails.getPayDate());

        return payrollRepository.save(payroll);
    }

    // Supprimer une fiche de paie
    public void deletePayroll(Long payrollId) {
        if (!payrollRepository.existsById(payrollId)) {
            throw new RuntimeException("Payroll non trouvé avec l'id " + payrollId);
        }
        payrollRepository.deleteById(payrollId);
    }

    // Récupérer toutes les fiches de paie avec infos employé (pour affichage)
    public List<PayrollDTO> getAllPayrollsWithEmployeeInfo() {
        return payrollRepository.findAll().stream().map(payroll -> {
            PayrollDTO dto = new PayrollDTO();
            dto.setPayrollId(payroll.getPayrollId());

            // --- Correction ici : Cast explicite en (double) ---
            dto.setBasicSalary((double) payroll.getBasicSalary());
            dto.setBonuses((double) payroll.getBonuses());
            dto.setDeductions((double) payroll.getDeductions());
            dto.setTotalSalary((double) payroll.getTotalSalary());
            // --- Fin de la correction ---

            dto.setPayDate(payroll.getPayDate());

            // Vérification de nullité pour l'employé
            if (payroll.getEmployee() != null) {
                Employee employee = payroll.getEmployee(); // Pour la lisibilité
                dto.setEmployeeId(employee.getId());
                dto.setEmployeeName(employee.getName() + " " + employee.getLastName());
                dto.setEmployeeEmail(employee.getEmail());

                // Vérification de nullité pour le département de l'employé
                if (employee.getDepartment() != null) {
                    dto.setEmployeeDepartment(employee.getDepartment().getDepartmentName());
                } else {
                    dto.setEmployeeDepartment("N/A"); // Ou null si vous préférez
                }
            } else {
                // Gérer le cas où la fiche de paie n'a pas d'employé associé
                dto.setEmployeeId(null);
                dto.setEmployeeName("Employé non associé");
                dto.setEmployeeEmail(null);
                dto.setEmployeeDepartment("N/A");
            }

            return dto;
        }).collect(Collectors.toList());
    }
}