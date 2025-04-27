package com.bezkoder.springjwt.HRModuleServices;

// Import the entity directly
import com.bezkoder.springjwt.dtos.HRModuleDtos.ContractRequestDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.Contract;
import com.bezkoder.springjwt.models.HRModuleEntities.ContractType;
import com.bezkoder.springjwt.models.HRModuleEntities.ContractStatus; // Import Statut enum
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.ContractRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation; // For manual validation
import jakarta.validation.ConstraintViolationException; // For manual validation
import jakarta.validation.Validator; // For manual validation
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Use transactions
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate; // Use LocalDate
import java.util.List;
import java.util.Optional;
import java.util.Set; // Import Set
import java.util.stream.Collectors;

@Service
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    // Inject Validator for manual entity validation
    @Autowired
    private Validator validator;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class FileData {
        private byte[] content;
        private String fileName;
        private String fileType;
    }
    @Transactional(readOnly = true)
    public List<Contract> getAllContracts() {
        return contractRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Contract> getContractById(Long id) {
        return contractRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Contract> getContractsByEmployeeId(Long employeeId) {
        // ... existing code ...
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));
        return employee.getContracts(); // Make sure Employee has getContracts()
    }


    @Transactional
    // *** CHANGE PARAMETER TYPE TO DTO ***
    public Contract createContract(ContractRequestDTO contractDto, Optional<MultipartFile> file) throws IOException {
        // --- Server-side Validation (DTO validation happens via @Valid in controller) ---
        // Additional validation if needed beyond DTO annotations

        // 1. Look up and associate the Employee entity using the ID from the DTO
        // DTO validation already ensures employeeId is not null if @NotNull
        Long employeeId = contractDto.getEmployeeId();
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        // 2. Create a new Contract entity and map fields from the DTO
        Contract contract = new Contract(); // Create a new entity instance

        // Map fields from DTO to Entity
        try {
            contract.setContractType(contractDto.getContractType()); // Already validated by DTO @NotNull
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ContractType value: " + contractDto.getContractType(), e);
        }

        try {
            contract.setStatut(contractDto.getStatut()); // Already validated by DTO @NotNull
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ContractStatus value: " + contractDto.getStatut(), e);
        }

        contract.setStartDate(contractDto.getStartDate()); // Already validated by DTO @NotNull
        contract.setEndDate(contractDto.getEndDate()); // Can be null
        contract.setRenewalDate(contractDto.getRenewalDate()); // Can be null
        contract.setReference(contractDto.getReference()); // Already validated by DTO @NotBlank
        contract.setDescription(contractDto.getDescription()); // Can be null

        // Set the Employee entity on the Contract entity
        contract.setEmployee(employee);


        // --- Handle File Upload ---
        if (file.isPresent() && !file.get().isEmpty()) {
            MultipartFile uploadedFile = file.get();
            // Basic validation for PDF type
            if (!"application/pdf".equals(uploadedFile.getContentType())) {
                throw new IllegalArgumentException("Seuls les fichiers PDF sont autorisés.");
            }
            // Optional: Add file size validation here if needed

            contract.setFileName(uploadedFile.getOriginalFilename());
            contract.setFileType(uploadedFile.getContentType());
            contract.setFileContent(uploadedFile.getBytes()); // Reading bytes can throw IOException
        } else {
            // No file provided, ensure file fields are null
            contract.setFileName(null);
            contract.setFileType(null);
            contract.setFileContent(null);
        }


        // 3. Validate the entity using Jakarta Validation annotations (if any apply after mapping)
        // DTO validation happens before this. This is for entity-specific constraints
        Set<ConstraintViolation<Contract>> violations = validator.validate(contract);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new ConstraintViolationException("Validation failed for Contract: " + errorMsg, violations);
        }

        // 4. Save the contract
        return contractRepository.save(contract);
    }

    @Transactional
    // *** CHANGE PARAMETER TYPE TO DTO ***
    public Contract updateContract(Long id, ContractRequestDTO contractDetailsDto, Optional<MultipartFile> file) throws IOException {
        // --- Server-side Validation (DTO validation happens via @Valid in controller) ---
        // Additional validation if needed

        // 1. Find the existing contract
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found with ID: " + id));

        // 2. Update allowed fields from the DTO onto the fetched entity
        // DTO validation already checked required fields in DTO

        // Do NOT allow changing the employee via this update method if EmployeeId is in the DTO
        // DTO validation already ensures employeeId is not null if @NotNull
        Long employeeIdFromDto = contractDetailsDto.getEmployeeId();
        if (employeeIdFromDto != null && !contract.getEmployee().getId().equals(employeeIdFromDto)) {
            // Optional: log warning or throw error if changing employee via update is forbidden
            System.out.println("Attempt to change employee on existing contract ID " + id + " ignored. DTO Employee ID: " + employeeIdFromDto + ", Existing Employee ID: " + contract.getEmployee().getId());
            // If changing employee IS allowed, you would look up the new employee entity and set it here.
            // For now, we are assuming changing employee via update is NOT allowed by this endpoint.
        }
        // Keep the original employee association
        // contract.setEmployee(contract.getEmployee()); // This is already the case as we fetched the existing entity


        // Update fields from DTO
        try {
            if (contractDetailsDto.getContractType() != null) {
                contract.setContractType(contractDetailsDto.getContractType());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ContractType value: " + contractDetailsDto.getContractType(), e);
        }

        try {
            if (contractDetailsDto.getStatut() != null) {
                contract.setStatut(contractDetailsDto.getStatut());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid ContractStatus value: " + contractDetailsDto.getStatut(), e);
        }

        contract.setStartDate(contractDetailsDto.getStartDate()); // Already validated by DTO @NotNull
        contract.setEndDate(contractDetailsDto.getEndDate()); // Can be null
        contract.setRenewalDate(contractDetailsDto.getRenewalDate()); // Can be null
        contract.setReference(contractDetailsDto.getReference()); // Already validated by DTO @NotBlank
        contract.setDescription(contractDetailsDto.getDescription()); // Can be null


        // --- Handle File Update ---
        if (file.isPresent() && !file.get().isEmpty()) {
            MultipartFile uploadedFile = file.get();
            // Basic validation for PDF type
            if (!"application/pdf".equals(uploadedFile.getContentType())) {
                throw new IllegalArgumentException("Seuls les fichiers PDF sont autorisés.");
            }
            // Optional: Add file size validation here if needed

            contract.setFileName(uploadedFile.getOriginalFilename());
            contract.setFileType(uploadedFile.getContentType());
            contract.setFileContent(uploadedFile.getBytes()); // Reading bytes can throw IOException
        }
        // Note: If file is Optional.empty() or present but empty,
        // the existing file data (fileContent, fileName, fileType) is *kept*.
        // If you need a way to *remove* the file without replacing it,
        // you'd need a separate flag in the DTO (e.g., `boolean removeFile`).


        // 3. Validate the updated entity
        // This is for entity-specific constraints beyond DTO
        Set<ConstraintViolation<Contract>> violations = validator.validate(contract);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new ConstraintViolationException("Validation failed for Contract update: " + errorMsg, violations);
        }


        // 4. Save the updated contract
        return contractRepository.save(contract);
    }

    @Transactional
    public void deleteContract(Long id) {
        if (!contractRepository.existsById(id)) {
            throw new EntityNotFoundException("Contract not found with ID: " + id);
        }
        contractRepository.deleteById(id);
    }

    // --- New method to download the PDF file ---
    @Transactional(readOnly = true) // Transaction needed to access LAZY loaded fileContent
    public FileData downloadContractPdf(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found with ID: " + contractId));

        if (contract.getFileContent() == null || contract.getFileName() == null || contract.getFileType() == null) {
            throw new IllegalStateException("No PDF file associated with this contract.");
        }

        // Accessing getFileContent() inside the @Transactional method triggers loading
        return new FileData(contract.getFileContent(), contract.getFileName(), contract.getFileType());
    }
    // --- End of new method ---
}