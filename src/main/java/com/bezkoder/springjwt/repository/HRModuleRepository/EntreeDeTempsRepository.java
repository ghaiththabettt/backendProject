package com.bezkoder.springjwt.repository.HRModuleRepository;

import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.HRModuleEntities.EntreeDeTemps;
import com.bezkoder.springjwt.models.HRModuleEntities.RestrictionsHorloge;
import com.bezkoder.springjwt.models.HRModuleEntities.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntreeDeTempsRepository extends JpaRepository<EntreeDeTemps, Long> {

    boolean existsByEmployeeIdAndStatusIsNot(Long employeeId, Status status);

    // Trouver le dernier pointage (peu importe l'employé) pour obtenir la restriction globale
    Optional<EntreeDeTemps> findTopByOrderByCreatedAtDesc();

    // Trouver un pointage en cours (non terminé et non en pause) pour un employé donné
    Optional<EntreeDeTemps> findByEmployeeIdAndStatus(Long employeeId, Status status);
    Optional<EntreeDeTemps> findByEmployeeIdAndStatusIsNot(Long employeeId, Status status); // Cherche non 'Termine'


    // Trouver les pointages d'un employé sur une période donnée
    List<EntreeDeTemps> findByEmployeeIdAndHeureDebutBetweenOrderByHeureDebutAsc(Long employeeId, LocalDateTime start, LocalDateTime end);

    // Trouver tous les pointages pour un employé
    List<EntreeDeTemps> findByEmployeeIdOrderByHeureDebutDesc(Long employeeId);

    // Vérifier si un pointage existe pour un employé pour un jour donné (Mode Strict/Modere)
    @Query("SELECT COUNT(e) > 0 FROM EntreeDeTemps e WHERE e.employee.id = :employeeId AND FUNCTION('DATE', e.heureDebut) = FUNCTION('DATE', :date)")
    boolean existsByEmployeeIdAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDateTime date);


}