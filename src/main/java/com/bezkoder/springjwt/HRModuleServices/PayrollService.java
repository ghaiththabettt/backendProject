package com.bezkoder.springjwt.HRModuleServices;

import com.bezkoder.springjwt.dtos.HRModuleDtos.PayrollDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.Payroll;
import com.bezkoder.springjwt.repository.HRModuleRepository.PayrollRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    @Autowired
    private PayrollRepository payrollRepository;

    // Récupérer tous les enregistrements
    public List<Payroll> getAllPayrolls() {
        return payrollRepository.findAll();
    }

    // Récupérer un enregistrement par son identifiant
    public Optional<Payroll> getPayrollById(Long payrollId) {
        return payrollRepository.findById(payrollId);
    }

    // Créer une nouvelle fiche de paie
    public Payroll createPayroll(Payroll payroll) {
        // Exemple de calcul du salaire total (basicSalary + bonuses - deductions)
        payroll.setTotalSalary(payroll.getBasicSalary() + payroll.getBonuses() - payroll.getDeductions());
        return payrollRepository.save(payroll);
    }

    // Mettre à jour une fiche de paie existante
    public Payroll updatePayroll(Long payrollId, Payroll payrollDetails) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll non trouvé avec l'id " + payrollId));
        payroll.setBasicSalary(payrollDetails.getBasicSalary());
        payroll.setBonuses(payrollDetails.getBonuses());
        payroll.setDeductions(payrollDetails.getDeductions());
        payroll.setTotalSalary(payrollDetails.getBasicSalary() + payrollDetails.getBonuses() - payrollDetails.getDeductions());
        payroll.setPayDate(payrollDetails.getPayDate());
        payroll.setEmployee(payrollDetails.getEmployee());
        return payrollRepository.save(payroll);
    }

    // Supprimer une fiche de paie
    public void deletePayroll(Long payrollId) {
        payrollRepository.deleteById(payrollId);
    }

    public List<PayrollDTO> getAllPayrollsWithEmployeeInfo() {
        return payrollRepository.findAll().stream().map(payroll -> {
            PayrollDTO dto = new PayrollDTO();
            dto.setPayrollId(payroll.getPayrollId());
            dto.setBasicSalary((double) payroll.getBasicSalary());
            dto.setBonuses((double) payroll.getBonuses());
            dto.setDeductions((double) payroll.getDeductions());
            dto.setTotalSalary((double) payroll.getTotalSalary());
            dto.setPayDate(payroll.getPayDate());

            dto.setEmployeeId(payroll.getEmployee().getId());
            dto.setEmployeeName(payroll.getEmployee().getName() + " " + payroll.getEmployee().getLastName());
            dto.setEmployeeEmail(payroll.getEmployee().getEmail());
            dto.setEmployeeDepartment(payroll.getEmployee().getDepartment() != null ?
                    payroll.getEmployee().getDepartment().getDepartmentName() : null);

            return dto;
        }).collect(Collectors.toList());
    }

}
