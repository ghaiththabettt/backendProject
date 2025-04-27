package com.bezkoder.springjwt.repository;

import com.bezkoder.springjwt.dtos.HRModuleDtos.EmployeeListDTO;
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.EEmployeePosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    List<Employee> findByPosition(EEmployeePosition position);
    Boolean existsByEmail(String email);
    List<Employee> findByDepartment_DepartmentId(Long departmentId);
    @Query("SELECT new com.bezkoder.springjwt.dtos.HRModuleDtos.EmployeeListDTO(e.id, CONCAT(e.name, ' ', e.lastName)) FROM Employee e ORDER BY e.name ASC, e.lastName ASC")
    List<EmployeeListDTO> findEmployeeList();


}
