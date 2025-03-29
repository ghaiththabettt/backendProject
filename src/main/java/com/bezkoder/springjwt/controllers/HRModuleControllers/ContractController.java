package com.bezkoder.springjwt.controllers.HRModuleControllers;

import com.bezkoder.springjwt.dtos.HRModuleDtos.ContractDTO;
import com.bezkoder.springjwt.HRModuleServices.ContractService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/Contract")
public class ContractController {
    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping
    public ContractDTO createContract(@RequestBody ContractDTO dto) {
        return contractService.createContract(dto);
    }

    @GetMapping
    public List<ContractDTO> getAllContracts() {
        return contractService.getAllContracts();
    }

    @GetMapping("/{id}")
    public Optional<ContractDTO> getContractById(@PathVariable Long id) {
        return contractService.getContractById(id);
    }

    @PutMapping("/{id}")
    public ContractDTO updateContract(@PathVariable Long id, @RequestBody ContractDTO dto) {
        return contractService.updateContract(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
    }
}

