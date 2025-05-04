package com.bezkoder.springjwt.HRModuleServices;
import com.bezkoder.springjwt.dtos.HRModuleDtos.SentimentDashboardDTO;
import com.bezkoder.springjwt.payload.response.PredictionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
// --- Keep existing imports ---
import com.bezkoder.springjwt.dtos.HRModuleDtos.LeaveDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.DurationType;
import com.bezkoder.springjwt.models.HRModuleEntities.Leave;
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.HRModuleEntities.LeaveType;
import com.bezkoder.springjwt.models.HRModuleEntities.StatusLeave;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.repository.HRModuleRepository.LeaveRepository;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import com.bezkoder.springjwt.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException; // Import AccessDeniedException
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.bezkoder.springjwt.dtos.HRModuleDtos.SentimentDashboardDTO; // Importer le nouveau DTO
import com.bezkoder.springjwt.dtos.HRModuleDtos.MotivationTrendPoint; // Importer le nouveau DTO interne

import java.time.Month; // Importer Month
import java.time.YearMonth; // Importer YearMonth
import java.util.ArrayList; // Importer ArrayList
import java.util.HashMap; // Importer HashMap
import java.util.Map; // Importer Map
import java.util.stream.Collectors; // Importer Collectors
import java.time.format.DateTimeFormatter;

@Service
public class LeaveService {
    private static final Logger log = LoggerFactory.getLogger(LeaveService.class);
    private static final int PENDING_LEAVE_STALE_THRESHOLD_DAYS = 3;
    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CongePredictionService congePredictionService;
    @Autowired
    private CongePredictionService predictionService;
    // --- Helper: Check if user is Admin or HR (Keep existing or add if missing) ---
    private boolean isAdminOrHr(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN") ||
                        grantedAuthority.getAuthority().equals("ROLE_HR"));
    }


    // --- Conversion Helpers (Keep existing convertToEntity and convertToDTO, ensure they use JOIN FETCH methods from repo) ---

    // Example Modification (Ensure uppercase enum conversion):
    private Leave convertToEntity(LeaveDTO dto, Leave existingLeave) {
        Leave leave = (existingLeave != null) ? existingLeave : new Leave();

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + dto.getEmployeeId()));
        leave.setEmployee(employee);

        // Ensure case-insensitive enum conversion and handle potential errors
        try {
            leave.setLeaveType(LeaveType.valueOf(dto.getLeaveType().toUpperCase()));
            leave.setDurationType(DurationType.valueOf(dto.getDurationType().toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException e) { // Catch null pointer too
            throw new IllegalArgumentException("Invalid or missing LeaveType/DurationType: " + dto.getLeaveType() + "/" + dto.getDurationType());
        }

        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        // Add date validation if needed (e.g., endDate >= startDate)

        leave.setNumberOfDays(dto.getNumberOfDays()); // Frontend might calculate this or backend recalculates
        leave.setReason(dto.getReason());
        leave.setNote(dto.getNote());
        leave.setSentiment(dto.getSentiment());

        // System set fields only on creation
        if (leave.getLeaveId() == null) {
            leave.setStatusLeave(StatusLeave.PENDING);
            leave.setRequestedOn(LocalDate.now());
            leave.setActionedBy(null);
            leave.setActionDate(null);
        }
        // Optional: Recalculate days in @PrePersist/@PreUpdate of Leave entity
        return leave;
    }

    // Keep existing convertToDTO
    private LeaveDTO convertToDTO(Leave leave) {
        LeaveDTO dto = new LeaveDTO();
        dto.setLeaveId(leave.getLeaveId());
        dto.setLeaveType(leave.getLeaveType().name());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setNumberOfDays(leave.getNumberOfDays());
        dto.setDurationType(leave.getDurationType().name());
        dto.setStatusLeave(leave.getStatusLeave().name());
        dto.setReason(leave.getReason());
        dto.setNote(leave.getNote());
        dto.setRequestedOn(leave.getRequestedOn());
        dto.setActionDate(leave.getActionDate());
        dto.setSentiment(leave.getSentiment());

        if (leave.getEmployee() != null) {
            dto.setEmployeeId(leave.getEmployee().getId());
            dto.setEmployeeName(leave.getEmployee().getName() + " " + leave.getEmployee().getLastName());
            if (leave.getEmployee().getDepartment() != null) {
                dto.setDepartmentName(leave.getEmployee().getDepartment().getDepartmentName());
            } else {
                dto.setDepartmentName("N/A");
            }
            dto.setEmployeeImg(null);
        } else {
            System.err.println("Warning: Employee is null for Leave ID: " + leave.getLeaveId());
        }

        System.out.println("--- Converting Leave ID: " + leave.getLeaveId() + " ---");
        System.out.println("Raw Action Date from Entity: " + leave.getActionDate());
        User actioner = leave.getActionedBy();
        if (actioner != null) {
            System.out.println("Actioned By User Object: PRESENT (ID: " + actioner.getId() + ")");
            try {
                String firstName = actioner.getName();
                String lastName = actioner.getLastName();
                System.out.println("Actioned By User Name: " + firstName);
                System.out.println("Actioned By User Last Name: " + lastName);
                if (firstName == null || lastName == null) {
                    System.err.println("Warning: User (ID: " + actioner.getId() + ") has null name/lastName.");
                    dto.setActionedByName("[Name Unavailable]");
                } else {
                    dto.setActionedByName(firstName + " " + lastName);
                }
                dto.setActionedById(actioner.getId());
            } catch (Exception e) { // Attrape toute exception ici pour le log
                System.err.println("Error accessing ActionedBy User details for Leave ID: " + leave.getLeaveId());
                e.printStackTrace();
                dto.setActionedById(null);
                dto.setActionedByName("[Access Error]");
            }
        } else {
            System.out.println("Actioned By User Object: NULL");
            dto.setActionedById(null);
            dto.setActionedByName(null);
        }
        System.out.println("DTO ActionDate assigned: " + dto.getActionDate());
        System.out.println("--- End Conversion for Leave ID: " + leave.getLeaveId() + " ---");

        return dto;
    }


    // --- CRUD and Action Methods ---

    @Transactional
    public LeaveDTO addLeave(LeaveDTO leaveDTO) {
        try {
            // 🔁 Appeler la prédiction si un "reason" est fourni
            if (leaveDTO.getReason() != null && !leaveDTO.getReason().isBlank()) {
                PredictionResponse prediction = congePredictionService.predictTypeCongeAndSentiment(leaveDTO.getReason());

                if (prediction != null) {
                    System.out.println("✅ Réponse Flask : " +
                            "Type = " + prediction.getType_conge_prevu() + ", " +
                            "Sentiment = " + prediction.getSentiment() + ", " +
                            "Interprétation = " + prediction.getInterpretation());

                    // 🧠 Si type_conge_prevu est nul ou vide, ne pas écraser
                    if (leaveDTO.getLeaveType() == null || leaveDTO.getLeaveType().isBlank()) {
                        leaveDTO.setLeaveType(prediction.getType_conge_prevu());
                    }

                    // ✅ Enregistrer l'interprétation dans le champ sentiment
                    leaveDTO.setSentiment(prediction.getInterpretation());
                } else {
                    System.err.println("❌ Erreur : réponse de prédiction nulle.");
                }
            }

            // Création de l'entité Leave
            Leave leave = convertToEntity(leaveDTO, null);
            Leave savedLeave = leaveRepository.save(leave);
            System.out.println("✅ Congé enregistré avec ID : " + savedLeave.getLeaveId());

            return convertToDTO(savedLeave);

        } catch (Exception e) {
            System.err.println("❌ Exception lors de l'enregistrement du congé : " + e.getMessage());
            e.printStackTrace();
            throw e; // Remonter l'exception pour qu'elle apparaisse dans les logs
        }
    }




    // For Admin/HR - Uses JOIN FETCH from repo
    public List<LeaveDTO> getAllLeaves() {
        return leaveRepository.findAllWithDetails()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // --- NEW Service Method: Get Leaves By Employee ID ---
    /**
     * Gets all leave requests for a specific employee ID.
     * Assumes authorization happened in the controller.
     * Requires a repository method findByEmployeeIdWithDetails.
     */
    public List<LeaveDTO> getLeavesByEmployeeId(Long employeeId) {
        // *** IMPORTANT: Add this method to your LeaveRepository if it doesn't exist ***
        // @Query("SELECT l FROM Leave l JOIN FETCH l.employee e LEFT JOIN FETCH l.actionedBy u LEFT JOIN FETCH e.department WHERE e.id = :employeeId ORDER BY l.requestedOn DESC")
        // List<Leave> findByEmployeeIdWithDetails(@Param("employeeId") Long employeeId);

        // For now, using the basic findByEmployeeId - WILL CAUSE N+1 QUERIES
        // Strongly recommend adding the JOIN FETCH version to the repository
        List<Leave> leaves = leaveRepository.findByEmployeeId(employeeId);
        System.err.println("WARN: Using findByEmployeeId without JOIN FETCH in LeaveService.getLeavesByEmployeeId. Consider adding findByEmployeeIdWithDetails to LeaveRepository for performance.");

        return leaves.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // --- MODIFIED Service Method: Get Leave By ID with Authorization ---
    /**
     * Gets a specific leave request by ID, ensuring the current user is authorized.
     */
    public LeaveDTO getLeaveById(Long leaveId, Long currentUserId) {
        Leave leave = leaveRepository.findByIdWithDetails(leaveId) // Use fetch query from repo
                .orElseThrow(() -> new EntityNotFoundException("Leave not found with ID: " + leaveId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Authorization Check: Allow if user is owner OR Admin/HR
        boolean isOwner = leave.getEmployee() != null && leave.getEmployee().getId().equals(currentUserId);
        boolean hasAdminHrRole = isAdminOrHr(authentication);

        if (!isOwner && !hasAdminHrRole) {
            throw new AccessDeniedException("You are not authorized to view this leave request.");
        }

        return convertToDTO(leave);
    }

    // --- MODIFIED Service Method: Update Leave with Authorization ---
    /**
     * Updates a leave request, ensuring the user is the owner and status is PENDING.
     */
    @Transactional
    public LeaveDTO updateLeave(Long leaveId, LeaveDTO leaveDTO, Long currentUserId) {
        Leave existingLeave = leaveRepository.findByIdWithDetails(leaveId) // Use fetch query
                .orElseThrow(() -> new EntityNotFoundException("Leave not found with ID: " + leaveId));

        // --- Authorization Checks ---
        // 1. Ownership Check
        if (existingLeave.getEmployee() == null || !existingLeave.getEmployee().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to update this leave request (Not Owner).");
        }
        // 2. Status Check
        if (existingLeave.getStatusLeave() != StatusLeave.PENDING) {
            throw new IllegalStateException("Cannot update a leave request that is not in PENDING status. Current status: " + existingLeave.getStatusLeave());
        }
        // --- End Authorization Checks ---

        // Ensure employee cannot be changed during update & set for converter
        if (leaveDTO.getEmployeeId() == null || !existingLeave.getEmployee().getId().equals(leaveDTO.getEmployeeId())) {
            // Silently correct the employee ID to prevent accidental/malicious changes
            leaveDTO.setEmployeeId(existingLeave.getEmployee().getId());
        }

        // Update allowed fields using the converter, passing the existing entity
        Leave updatedLeaveData = convertToEntity(leaveDTO, existingLeave);

        Leave savedLeave = leaveRepository.save(updatedLeaveData);
        return convertToDTO(savedLeave);
    }

    // --- MODIFIED Service Method: Delete Leave with Authorization ---
    /**
     * Deletes a leave request, ensuring the user is the owner and status is PENDING.
     */
    @Transactional
    public void deleteLeave(Long leaveId, Long currentUserId) {
        Leave leave = leaveRepository.findByIdWithDetails(leaveId) // Use fetch query
                .orElseThrow(() -> new EntityNotFoundException("Leave not found with ID: " + leaveId));

        // --- Authorization Checks ---
        // 1. Ownership Check
        if (leave.getEmployee() == null || !leave.getEmployee().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to delete this leave request (Not Owner).");
        }
        // 2. Status Check
        if (leave.getStatusLeave() != StatusLeave.PENDING) {
            throw new IllegalStateException("Cannot delete a leave request that is not in PENDING status.");
        }
        // --- End Authorization Checks ---

        leaveRepository.deleteById(leaveId);
    }

    // --- Admin/HR Actions (Approve/Reject) - Ensure they use findByIdWithDetails ---

    @Transactional
    public LeaveDTO approveLeave(Long leaveId, Long actionUserId) {
        Leave leave = leaveRepository.findByIdWithDetails(leaveId) // Use fetch query
                .orElseThrow(() -> new EntityNotFoundException("Leave not found with ID: " + leaveId));
        User actionUser = userRepository.findById(actionUserId)
                .orElseThrow(() -> new EntityNotFoundException("User performing action not found with ID: " + actionUserId));

        // Can only approve PENDING requests
        if (leave.getStatusLeave() != StatusLeave.PENDING) {
            throw new IllegalStateException("Leave request is not in PENDING status. Current status: " + leave.getStatusLeave());
        }

        leave.setStatusLeave(StatusLeave.APPROVED);
        leave.setActionedBy(actionUser);
        leave.setActionDate(LocalDate.now());
        Leave savedLeave = leaveRepository.save(leave);
        return convertToDTO(savedLeave);
    }

    @Transactional
    public LeaveDTO rejectLeave(Long leaveId, Long actionUserId) {
        Leave leave = leaveRepository.findByIdWithDetails(leaveId) // Use fetch query
                .orElseThrow(() -> new EntityNotFoundException("Leave not found with ID: " + leaveId));
        User actionUser = userRepository.findById(actionUserId)
                .orElseThrow(() -> new EntityNotFoundException("User performing action not found with ID: " + actionUserId));

        // Can only reject PENDING requests
        if (leave.getStatusLeave() != StatusLeave.PENDING) {
            throw new IllegalStateException("Leave request is not in PENDING status. Current status: " + leave.getStatusLeave());
        }

        leave.setStatusLeave(StatusLeave.REJECTED);
        leave.setActionedBy(actionUser);
        leave.setActionDate(LocalDate.now());
        Leave savedLeave = leaveRepository.save(leave);
        return convertToDTO(savedLeave);
    }



    @Transactional(readOnly = true) // Transaction pour lire les données
    public SentimentDashboardDTO getSentimentDashboardData() {
        log.info("Fetching sentiment dashboard data...");

        // Utiliser la méthode optimisée si elle existe, sinon charger toutes les entités
        // List<Object[]> sentimentData = leaveRepository.findAllSentimentsAndDates();
        // Ou charger toutes les entités (moins performant si beaucoup de données)
        List<Leave> allLeaves = leaveRepository.findAll(); // Assurez-vous que 'sentiment' est chargé (FetchType.EAGER ou via query)

        // Filtrer ceux qui ont un sentiment enregistré
        List<Leave> leavesWithSentiment = allLeaves.stream()
                .filter(leave -> leave.getSentiment() != null && !leave.getSentiment().isBlank() && leave.getRequestedOn() != null)
                .collect(Collectors.toList());

        log.debug("Found {} leaves with sentiment data.", leavesWithSentiment.size());

        // 1. Calculer les comptes par sentiment (pour le Pie Chart)
        Map<String, Long> sentimentCounts = leavesWithSentiment.stream()
                .collect(Collectors.groupingBy(Leave::getSentiment, Collectors.counting()));

        log.debug("Sentiment counts calculated: {}", sentimentCounts);

        // 2. Calculer la tendance de motivation (pour le Line Chart)
        // Regrouper par mois (Année-Mois)
        Map<YearMonth, List<Leave>> leavesByMonth = leavesWithSentiment.stream()
                .collect(Collectors.groupingBy(leave -> YearMonth.from(leave.getRequestedOn())));

        // Calculer un score moyen par mois
        List<MotivationTrendPoint> motivationTrend = new ArrayList<>();
        leavesByMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // Trier par mois
                .forEach(entry -> {
                    YearMonth month = entry.getKey();
                    List<Leave> monthlyLeaves = entry.getValue();
                    double monthlyScore = calculateMonthlyMotivationScore(monthlyLeaves);
                    // Formatter le mois en "YYYY-MM" pour ApexCharts
                    String periodLabel = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    motivationTrend.add(new MotivationTrendPoint(periodLabel, monthlyScore));
                });

        log.debug("Motivation trend calculated with {} points.", motivationTrend.size());

        return new SentimentDashboardDTO(sentimentCounts, motivationTrend);
    }

    /**
     * Calcule un score de "motivation" simple basé sur les sentiments des congés d'un mois.
     * ATTENTION : C'est une simplification extrême ! La "motivation" est très complexe.
     *             Ceci est juste un indicateur basé sur le langage utilisé.
     * @param monthlyLeaves La liste des congés pour un mois donné.
     * @return Un score moyen (ex: -2 Très Négatif, -1 Négatif, 0 Neutre, 1 Positif, 2 Très Positif).
     */
    private double calculateMonthlyMotivationScore(List<Leave> monthlyLeaves) {
        if (monthlyLeaves == null || monthlyLeaves.isEmpty()) {
            return 0.0; // Ou une autre valeur par défaut
        }

        double totalScore = 0;
        for (Leave leave : monthlyLeaves) {
            totalScore += getScoreForSentiment(leave.getSentiment());
        }

        return totalScore / monthlyLeaves.size(); // Score moyen
    }

    /**
     * Attribue une valeur numérique à chaque interprétation de sentiment.
     * Adaptez ces scores selon votre interprétation.
     */
    private int getScoreForSentiment(String sentimentInterpretation) {
        if (sentimentInterpretation == null) return 0;
        // Utiliser startsWith pour être robuste aux emojis
        if (sentimentInterpretation.startsWith("🟢 Très positif")) return 2;
        if (sentimentInterpretation.startsWith("🟢 Positif")) return 1;
        if (sentimentInterpretation.startsWith("🟡 Neutre")) return 0;
        if (sentimentInterpretation.startsWith("🟠 Négatif")) return -1;
        if (sentimentInterpretation.startsWith("🔴 Très négatif")) return -2;
        return 0; // Score par défaut pour "Inconnu" ou autre
    }
    //  @Scheduled(cron = "0 */ // 2 * * * *")
   /* @Transactional(readOnly = true) // Bon pour les tâches de lecture seule
    public void checkApprovedLeaveDateStatus() {
        log.info("--- Démarrage Tâche Planifiée: Vérification Statut Congés Approuvés à {} ---", LocalDateTime.now());
        LocalDate today = LocalDate.now();

        try {
            // Récupérer uniquement les congés qui sont déjà approuvés
            List<Leave> approvedLeaves = leaveRepository.findByStatusLeave(StatusLeave.APPROVED);
            // Ou utilisez findByStatusLeaveWithEmployee si vous avez besoin de l'employé pour le log

            if (approvedLeaves.isEmpty()) {
                log.info("Aucun congé avec le statut APPROVED trouvé pour vérification.");
            } else {
                log.info("Vérification de {} congé(s) avec le statut APPROVED :", approvedLeaves.size());
                for (Leave leave : approvedLeaves) {
                    String interpretedState;
                    // 1. Vérifier si le congé est dans le futur
                    if (today.isBefore(leave.getStartDate())) {
                        interpretedState = String.format("À venir (Début: %s)", leave.getStartDate());
                    }
                    else if (!today.isAfter(leave.getEndDate())) { // today >= startDate ET today <= endDate
                        interpretedState = String.format("En cours (Période: %s au %s)", leave.getStartDate(), leave.getEndDate());
                    }
                    // 3. Sinon, le congé est terminé
                    else {
                        interpretedState = String.format("Terminé (Fin: %s)", leave.getEndDate());
                    }

                    // Logger le résultat de la vérification
                    log.info("  - Congé ID: {}, Employé ID: {}, Statut: {}, État Actuel: {}",
                            leave.getLeaveId(),
                            (leave.getEmployee() != null ? leave.getEmployee().getId() : "N/A"), // Accès sécurisé à l'ID employé
                            leave.getStatusLeave(), // Sera toujours APPROVED ici
                            interpretedState);
                }
            }

        } catch (Exception e) {
            // Loguer toute erreur survenant pendant l'exécution de la tâche
            log.error("Erreur durant l'exécution de la tâche planifiée 'checkApprovedLeaveDateStatus': {}", e.getMessage(), e);
        } finally {
            log.info("--- Fin Tâche Planifiée: Vérification Statut Congés Approuvés ---");
        }
    } */
}