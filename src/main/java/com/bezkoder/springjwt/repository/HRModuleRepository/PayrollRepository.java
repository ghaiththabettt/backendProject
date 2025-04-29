package com.bezkoder.springjwt.repository.HRModuleRepository;

import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.HRModuleEntities.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findByEmployee(Employee employee);

    // Trouver les fiches de paie pour un employé par ID
    List<Payroll> findByEmployeeId(Long employeeId);

    // Trouver une fiche de paie pour un employé à une date de paiement spécifique (si unique)
    Optional<Payroll> findByEmployeeAndPayDate(Employee employee, LocalDate payDate);

    // Trouver les fiches de paie dans une période de date de paiement
    List<Payroll> findByPayDateBetween(LocalDate startDate, LocalDate endDate);
}



