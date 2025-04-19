package com.bezkoder.springjwt.repository.HRModuleRepository;

import com.bezkoder.springjwt.models.HRModuleEntities.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByEmployeeId(Long employeeId);

    // *** NOUVELLE MÉTHODE POUR CHARGER AVEC LES RELATIONS ***
    @Query("SELECT lv FROM Leave lv " +
            "LEFT JOIN FETCH lv.employee emp " +       // Charge l'employé associé
            "LEFT JOIN FETCH emp.department dept " + // Optionnel: charge le département de l'employé si besoin dans DTO
            "LEFT JOIN FETCH lv.actionedBy actBy")
    // Charge l'utilisateur ayant effectué l'action (approbateur/rejeteur)
    List<Leave> findAllWithDetails(); // Nouveau nom de méthode descriptif

    // Vous pouvez aussi faire un findById avec JOIN FETCH si nécessaire pour getLeaveById
    @Query("SELECT lv FROM Leave lv " +
            "LEFT JOIN FETCH lv.employee emp " +
            "LEFT JOIN FETCH emp.department dept " +
            "LEFT JOIN FETCH lv.actionedBy actBy " +
            "WHERE lv.leaveId = :leaveId")
    Optional<Leave> findByIdWithDetails(Long leaveId);
}