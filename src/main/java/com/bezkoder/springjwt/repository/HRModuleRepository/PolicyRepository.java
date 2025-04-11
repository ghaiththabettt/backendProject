package com.bezkoder.springjwt.repository.HRModuleRepository;

import com.bezkoder.springjwt.dtos.HRModuleDtos.PolicyDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    @Query("SELECT new com.bezkoder.springjwt.dtos.HRModuleDtos.PolicyDTO(p.policyId, p.title, p.description, p.department.departmentId) " +
            "FROM Policy p WHERE p.department.departmentId = :departmentId")
    List<PolicyDTO> findByDepartmentId(@Param("departmentId") Long departmentId);

}
