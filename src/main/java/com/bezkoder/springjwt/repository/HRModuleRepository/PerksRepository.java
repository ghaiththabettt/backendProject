package com.bezkoder.springjwt.repository.HRModuleRepository;

import com.bezkoder.springjwt.models.HRModuleEntities.Perks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerksRepository extends JpaRepository<Perks, Long> {

}
