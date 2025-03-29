package com.bezkoder.springjwt.repository.HRModuleRepository;

import com.bezkoder.springjwt.models.HRModuleEntities.TimeSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeSheetRepository extends JpaRepository<TimeSheet, Long> {

    List<TimeSheet> findByEmployeeId(Long employeeId);
}
