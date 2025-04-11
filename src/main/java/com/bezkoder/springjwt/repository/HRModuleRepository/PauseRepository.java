package com.bezkoder.springjwt.repository.HRModuleRepository;

import com.bezkoder.springjwt.models.HRModuleEntities.Pause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PauseRepository extends JpaRepository<Pause, Long> {
    // Trouver la dernière pause non terminée pour une entrée de temps
    Optional<Pause> findTopByEntreeDeTempsIdAndFinIsNullOrderByDebutDesc(Long entreeDeTempsId);
}
