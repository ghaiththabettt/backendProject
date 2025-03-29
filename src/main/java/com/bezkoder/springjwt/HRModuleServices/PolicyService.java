package com.bezkoder.springjwt.HRModuleServices;



import com.bezkoder.springjwt.models.HRModuleEntities.Department;
import com.bezkoder.springjwt.models.HRModuleEntities.Policy;
import com.bezkoder.springjwt.dtos.HRModuleDtos.PolicyDTO;
import com.bezkoder.springjwt.repository.HRModuleRepository.PolicyRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public PolicyService(PolicyRepository policyRepository, DepartmentRepository departmentRepository) {
        this.policyRepository = policyRepository;
        this.departmentRepository = departmentRepository;
    }

    // Créer une nouvelle policy
    public Policy createPolicy(PolicyDTO policyDTO) {
        Policy policy = new Policy();
        policy.setTitle(policyDTO.getPolicyName());
        policy.setDescription(policyDTO.getDescription());
        policy.setCreatedDate(LocalDate.now());
        policy.setUpdatedDate(LocalDate.now());

        // Associer le département (si nécessaire)
        Optional<Department> department = departmentRepository.findById(policyDTO.getDepartmentId());
        department.ifPresent(policy::setDepartment);

        return policyRepository.save(policy);
    }

    // Mettre à jour une policy existante
    public Policy updatePolicy(Long policyId, PolicyDTO policyDTO) {
        Optional<Policy> policyOptional = policyRepository.findById(policyId);
        if (policyOptional.isPresent()) {
            Policy policy = policyOptional.get();
            policy.setTitle(policyDTO.getPolicyName());
            policy.setDescription(policyDTO.getDescription());
            policy.setUpdatedDate(LocalDate.now());

            return policyRepository.save(policy);
        }
        return null;  // Ou gérer le cas où la policy n'existe pas
    }

    // Récupérer une policy par ID
    public Policy getPolicyById(Long policyId) {
        return policyRepository.findById(policyId).orElse(null);
    }

    // Récupérer toutes les policies
    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    // Récupérer toutes les policies sous forme de DTO
    public List<PolicyDTO> getAllPolicyDTOs() {
        return policyRepository.findAll().stream()
                .map(policy -> {
                    PolicyDTO dto = new PolicyDTO();
                    dto.setPolicyId(policy.getPolicyId());
                    dto.setPolicyName(policy.getTitle());
                    dto.setDescription(policy.getDescription());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Supprimer une policy par ID
    public boolean deletePolicy(Long policyId) {
        Optional<Policy> policyOptional = policyRepository.findById(policyId);
        if (policyOptional.isPresent()) {
            policyRepository.delete(policyOptional.get());
            return true;
        }
        return false;
    }
}
