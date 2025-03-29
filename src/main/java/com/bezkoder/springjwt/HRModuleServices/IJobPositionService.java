package com.bezkoder.springjwt.HRModuleServices;

import com.bezkoder.springjwt.models.HRModuleEntities.JobPosition;
import java.util.List;

public interface IJobPositionService {
    JobPosition addJobPosition(JobPosition jobPosition);
    JobPosition updateJobPosition(JobPosition jobPosition);
    void deleteJobPosition(Long id);
    JobPosition getJobPositionById(Long id);
    List<JobPosition> getAllJobPositions();
    List<JobPosition> getJobPositionsByDepartment(Long departmentId);
    List<JobPosition> getJobPositionsByJobLevel(String jobLevel);
}
