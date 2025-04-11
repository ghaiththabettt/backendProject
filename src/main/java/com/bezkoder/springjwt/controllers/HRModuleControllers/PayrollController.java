package com.bezkoder.springjwt.controllers.HRModuleControllers;

import com.bezkoder.springjwt.HRModuleServices.PayrollService;
import com.bezkoder.springjwt.dtos.HRModuleDtos.PayrollDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.Payroll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600) // Autorise les requêtes CORS depuis n'importe quelle origine

@RestController
@RequestMapping("/api/payrolls")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    // GET : récupérer toutes les fiches de paie
   /* @GetMapping("/get-all")
    @PreAuthorize("hasRole('USER') or hasRole('EMPLOYEE') or hasRole('ADMIN')")

    public List<Payroll> getAllPayrolls() {
        return payrollService.getAllPayrolls();
    }*/

    @GetMapping
    public ResponseEntity<List<PayrollDTO>> getAllPayrolls() {
        return ResponseEntity.ok(payrollService.getAllPayrollsWithEmployeeInfo());
    }

    // GET : récupérer une fiche de paie par son identifiant
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('EMPLOYEE') or hasRole('ADMIN')")

    public ResponseEntity<Payroll> getPayrollById(@PathVariable("id") Long payrollId) {
        Optional<Payroll> payroll = payrollService.getPayrollById(payrollId);
        return payroll.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST : créer une nouvelle fiche de paie
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')  or hasRole('ROLE_HR')")

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
