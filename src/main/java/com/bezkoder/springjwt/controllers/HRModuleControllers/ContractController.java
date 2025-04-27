package com.bezkoder.springjwt.controllers.HRModuleControllers;

import com.bezkoder.springjwt.dtos.HRModuleDtos.ContractRequestDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.Contract;
import com.bezkoder.springjwt.HRModuleServices.ContractService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders; // Import HttpHeaders
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // Import MediaType
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile

import java.io.IOException; // Import IOException
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/Contract")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @PostMapping
    //@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Object> createContract(
            // *** CHANGE PARAMETER TYPE TO DTO ***
            @RequestPart("contract") @Valid ContractRequestDTO contractDto, // Use the DTO here
            @RequestPart(value = "file", required = false) Optional<MultipartFile> file) {
        try {
            // Pass the DTO and Optional file to the service
            // The service will now handle mapping from DTO to Entity
            Contract createdContract = contractService.createContract(contractDto, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdContract);
        } // ... catch blocks remain similar, handling exceptions from service ...
        catch (IllegalArgumentException | EntityNotFoundException e) {
            System.err.println("Bad request for creating contract: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ConstraintViolationException e) {
            System.err.println("Validation errors creating contract: " + e.getMessage());
            // Extract and format violation messages if needed, or return default message
            String validationErrors = e.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(java.util.stream.Collectors.joining(", "));
            // ConstraintViolationException might come from DTO validation (@Valid)
            return ResponseEntity.badRequest().body("Validation failed: " + validationErrors);
        } catch (IOException e) {
            System.err.println("File handling error creating contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error creating contract: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal server error occurred while creating the contract.");
        }
    }

    @GetMapping
    //@PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('EMPLOYEE')")
    public ResponseEntity<Object> getAllContracts() {
        try {
            // Note: fileContent is LAZY loaded and won't be included in the response body unless explicitly accessed.
            List<Contract> contracts = contractService.getAllContracts();
            return ResponseEntity.ok(contracts);
        } catch (Exception e) {
            System.err.println("Error fetching all contracts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal server error occurred.");
        }
    }

    @GetMapping("/employee/{employeeId}")
    //@PreAuthorize("hasRole('ADMIN') or hasRole('HR') or hasRole('EMPLOYEE')")
    public ResponseEntity<Object> getContractsByEmployee(@PathVariable Long employeeId) {
        try {
            // TODO: Add security check if role is EMPLOYEE
            List<Contract> contracts = contractService.getContractsByEmployeeId(employeeId);
            // Note: fileContent is LAZY loaded and won't be included by default.
            return ResponseEntity.ok(contracts);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("Error fetching contracts for employee ID " + employeeId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal server error occurred.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contract> getContractById(@PathVariable Long id) {
        try {
            // findById might not eager load the @Lob LAZY fileContent.
            // If the client needs file metadata (name, type), findById is fine.
            // If the client needs the *content*, they must use the download endpoint.
            Optional<Contract> contractOpt = contractService.getContractById(id);

            Contract contract = contractOpt.orElseThrow(() -> new EntityNotFoundException("Contract not found with ID: " + id));

            // The returned contract object will have fileContent as null or uninitialized proxy
            // unless you explicitly access it or change fetch type (not recommended).
            return ResponseEntity.ok(contract);
        } catch (EntityNotFoundException e) {
            // Handle EntityNotFoundException directly here for this specific endpoint
            System.err.println("Contract not found for ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Or return e.getMessage() if return type is <Object>
        } catch (Exception e) {
            System.err.println("Error fetching contract ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Or return error message
        }
    }


    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Object> updateContract(
            @PathVariable Long id,
            // *** CHANGE PARAMETER TYPE TO DTO FOR UPDATE AS WELL ***
            @RequestPart("contract") @Valid ContractRequestDTO contractDetailsDto, // Use the DTO here
            @RequestPart(value = "file", required = false) Optional<MultipartFile> file) {
        try {
            // Pass ID, the DTO, and Optional file to the service
            // The service will now handle mapping from DTO to Entity for update
            Contract updatedContract = contractService.updateContract(id, contractDetailsDto, file);
            return ResponseEntity.ok(updatedContract);
        } catch (EntityNotFoundException e) {
            System.err.println("Contract not found for update ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | ConstraintViolationException e) {
            System.err.println("Validation/Illegal argument errors updating contract ID " + id + ": " + e.getMessage());
            String errorMsg = (e instanceof ConstraintViolationException) ?
                    ((ConstraintViolationException) e).getConstraintViolations().stream()
                            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                            .collect(java.util.stream.Collectors.joining(", ")) : e.getMessage();
            return ResponseEntity.badRequest().body(errorMsg);
        } catch (IOException e) {
            System.err.println("File handling error updating contract: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error updating contract ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal server error occurred while updating the contract.");
        }
    }

    @DeleteMapping("/{id}")
    //@PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        try {
            contractService.deleteContract(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            System.err.println("Contract not found for deletion ID " + id + ": " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error deleting contract ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- New endpoint for downloading the PDF file ---
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadContractPdf(@PathVariable Long id) {
        try {
            ContractService.FileData fileData = contractService.downloadContractPdf(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(fileData.getFileType())); // Set Content-Type header
            // Set Content-Disposition to trigger download
            headers.setContentDispositionFormData("attachment", fileData.getFileName()); // Suggest filename for download
            // Optional: Add Content-Length header
            headers.setContentLength(fileData.getContent().length);

            return new ResponseEntity<>(fileData.getContent(), headers, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            System.err.println("Contract not found for PDF download ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 with no body
        } catch (IllegalStateException e) {
            System.err.println("No PDF file found for contract ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 indicating no file
        } catch (Exception e) {
            System.err.println("Error downloading PDF for contract ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Return 500
        }
    }
    // --- End of new endpoint ---
}