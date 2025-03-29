package com.bezkoder.springjwt.repository.HRModuleRepository;

import com.bezkoder.springjwt.models.HRModuleEntities.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

}
