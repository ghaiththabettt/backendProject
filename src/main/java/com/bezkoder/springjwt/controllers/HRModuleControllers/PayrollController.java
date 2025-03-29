package com.bezkoder.springjwt.controllers.HRModuleControllers;

import com.bezkoder.springjwt.models.HRModuleEntities.Payroll;
import com.bezkoder.springjwt.security.services.HRModuleServices.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payrolls")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    // GET : récupérer toutes les fiches de paie
    @GetMapping
    public List<Payroll> getAllPayrolls() {
        return payrollService.getAllPayrolls();
    }

    // GET : récupérer une fiche de paie par son identifiant
    @GetMapping("/{id}")
    public ResponseEntity<Payroll> getPayrollById(@PathVariable("id") Long payrollId) {
        Optional<Payroll> payroll = payrollService.getPayrollById(payrollId);
        return payroll.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST : créer une nouvelle fiche de paie
    @PostMapping
    public Payroll createPayroll(@RequestBody Payroll payroll) {
        return payrollService.createPayroll(payroll);
    }

    // PUT : mettre à jour une fiche de paie existante
    @PutMapping("/{id}")
    public ResponseEntity<Payroll> updatePayroll(@PathVariable("id") Long payrollId,
                                                 @RequestBody Payroll payrollDetails) {
        try {
            Payroll updatedPayroll = payrollService.updatePayroll(payrollId, payrollDetails);
            return ResponseEntity.ok(updatedPayroll);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE : supprimer une fiche de paie
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayroll(@PathVariable("id") Long payrollId) {
        payrollService.deletePayroll(payrollId);
        return ResponseEntity.noContent().build();
    }
}
