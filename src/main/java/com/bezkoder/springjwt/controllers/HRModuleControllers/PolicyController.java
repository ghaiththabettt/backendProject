package com.bezkoder.springjwt.controllers.HRModuleControllers;


import com.bezkoder.springjwt.dtos.HRModuleDtos.PolicyDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.Policy;
import com.bezkoder.springjwt.HRModuleServices.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies")
@CrossOrigin(origins = "*") // Permet les requêtes CORS depuis n'importe quelle origine
public class PolicyController {

    private final PolicyService policyService;

    @Autowired
    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    // 🔹 Créer une nouvelle policy
    @PostMapping
    public ResponseEntity<Policy> createPolicy(@RequestBody PolicyDTO policyDTO) {
        Policy newPolicy = policyService.createPolicy(policyDTO);
        return ResponseEntity.ok(newPolicy);
    }

    // 🔹 Récupérer une policy par ID
    @GetMapping("/{id}")
    public ResponseEntity<Policy> getPolicyById(@PathVariable Long id) {
        Policy policy = policyService.getPolicyById(id);
        return policy != null ? ResponseEntity.ok(policy) : ResponseEntity.notFound().build();
    }

    // 🔹 Récupérer toutes les policies
    @GetMapping
    public ResponseEntity<List<PolicyDTO>> getAllPolicies() {
        List<PolicyDTO> policies = policyService.getAllPolicyDTOs();
        return ResponseEntity.ok(policies);
    }

    // 🔹 Mettre à jour une policy
    @PutMapping("/{id}")
    public ResponseEntity<Policy> updatePolicy(@PathVariable Long id, @RequestBody PolicyDTO policyDTO) {
        Policy updatedPolicy = policyService.updatePolicy(id, policyDTO);
        return updatedPolicy != null ? ResponseEntity.ok(updatedPolicy) : ResponseEntity.notFound().build();
    }

    // 🔹 Supprimer une policy
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        boolean deleted = policyService.deletePolicy(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /*@GetMapping("/department/{departmentId}")
    public ResponseEntity<List<PolicyDTO>> getPoliciesByDepartment(@PathVariable Long departmentId) {
        List<PolicyDTO> policies = policyService.getAllPolicyDTOs().stream()
                .filter(p -> p.getDepartmentId() != null && p.getDepartmentId().equals(departmentId))
                .toList();

        return ResponseEntity.ok(policies);
    }*/

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<PolicyDTO>> getPoliciesByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(policyService.getPoliciesByDepartmentId(departmentId));
    }




}
