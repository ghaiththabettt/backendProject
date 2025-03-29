package com.bezkoder.springjwt.repository.HRModuleRepository;

import com.bezkoder.springjwt.models.HRModuleEntities.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

}
