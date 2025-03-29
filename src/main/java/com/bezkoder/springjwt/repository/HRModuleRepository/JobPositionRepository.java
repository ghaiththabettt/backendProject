package com.bezkoder.springjwt.repository.HRModuleRepository;

import com.bezkoder.springjwt.models.HRModuleEntities.JobPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {
    List<JobPosition> findByDepartment_DepartmentId(Long departmentId);
    List<JobPosition> findByJobLevel(String jobLevel);
    List<JobPosition> findByTitleContainingIgnoreCase(String title);

}
