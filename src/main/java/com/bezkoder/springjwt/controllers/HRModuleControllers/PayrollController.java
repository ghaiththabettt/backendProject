package com.bezkoder.springjwt.controllers.HRModuleControllers;

import com.bezkoder.springjwt.HRModuleServices.PayrollService;
import com.bezkoder.springjwt.dtos.HRModuleDtos.PayrollDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.Payroll;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus; // Import pour HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/payrolls")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    // --- Endpoint pour générer les fiches de paie (Décommenté) ---
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')") // Sécuriser l'endpoint
    public ResponseEntity<?> generatePayrolls(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Dates de début et de fin invalides ou manquantes."));
        }

        try {
            payrollService.generatePayrollsForPeriod(startDate, endDate);
            return ResponseEntity.ok(new MessageResponse("Génération des fiches de paie pour la période " + startDate + " à " + endDate + " terminée avec succès."));
        } catch (Exception e) {
            // Log l'erreur côté serveur plus proprement
            System.err.printf("Erreur lors de la génération des fiches de paie pour la période %s - %s: %s%n", startDate, endDate, e.getMessage());
            e.printStackTrace(); // Pour le débogage en développement
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) // Utiliser HttpStatus
                    .body(new MessageResponse("Error: Une erreur interne est survenue lors de la génération des fiches de paie."));
        }
    }
    // --- Fin de l'endpoint de génération ---


    // GET : récupérer toutes les fiches de paie (utilise DTO pour affichage)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')") // Sécuriser cet endpoint aussi
    public ResponseEntity<List<PayrollDTO>> getAllPayrolls() {
        try {
            List<PayrollDTO> payrolls = payrollService.getAllPayrollsWithEmployeeInfo();
            return ResponseEntity.ok(payrolls);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de toutes les fiches de paie: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Retourner une erreur 500
        }
    }

    // GET : récupérer une fiche de paie par son identifiant (retourne l'entité brute)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR') or @payrollSecurityService.canAccessPayroll(#payrollId, principal)") // Exemple de sécurité plus fine
    public ResponseEntity<Payroll> getPayrollById(@PathVariable("id") Long payrollId) {
        Optional<Payroll> payroll = payrollService.getPayrollById(payrollId);
        return payroll.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
        // Note: Pourrait être mieux de retourner un PayrollDTO ici aussi pour la cohérence.
    }

    // POST : créer une nouvelle fiche de paie (Manuellement)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> createPayroll(@RequestBody Payroll payroll) {
        try {
            // Valider que l'employé existe si un ID est fourni
            if (payroll.getEmployee() == null || payroll.getEmployee().getId() == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Employee ID manquant."));
            }
            Payroll createdPayroll = payrollService.createPayroll(payroll);
            return new ResponseEntity<>(createdPayroll, HttpStatus.CREATED);
        } catch (RuntimeException e) { // Attrape l'erreur si l'employé n'est pas trouvé
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la fiche de paie: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error: Une erreur interne est survenue lors de la création."));
        }
    }

    // PUT : mettre à jour une fiche de paie existante (Manuellement)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> updatePayroll(@PathVariable("id") Long payrollId,
                                           @RequestBody Payroll payrollDetails) {
        try {
            Payroll updatedPayroll = payrollService.updatePayroll(payrollId, payrollDetails);
            return ResponseEntity.ok(updatedPayroll);
        } catch (RuntimeException e) { // Attrape Employee ou Payroll non trouvé
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            System.err.printf("Erreur lors de la mise à jour de la fiche de paie %d: %s%n", payrollId, e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error: Une erreur interne est survenue lors de la mise à jour."));
        }
    }

    // DELETE : supprimer une fiche de paie
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> deletePayroll(@PathVariable("id") Long payrollId) {
        try {
            payrollService.deletePayroll(payrollId);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (RuntimeException e) { // Attrape Payroll non trouvé
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            System.err.printf("Erreur lors de la suppression de la fiche de paie %d: %s%n", payrollId, e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error: Une erreur interne est survenue lors de la suppression."));
        }
    }
}