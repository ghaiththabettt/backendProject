package com.bezkoder.springjwt.HRModuleServices;


import com.bezkoder.springjwt.dtos.HRModuleDtos.ContractDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.Contract;
import com.bezkoder.springjwt.models.HRModuleEntities.ContractType;
import com.bezkoder.springjwt.repository.HRModuleRepository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    public List<ContractDTO> getAllContracts() {
        List<Contract> contracts = contractRepository.findAll();
        return contracts.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Optional<ContractDTO> getContractById(Long id) {
        return contractRepository.findById(id).map(this::convertToDTO);
    }

    public ContractDTO createContract(ContractDTO contractDTO) {
        Contract contract = convertToEntity(contractDTO);
        contract = contractRepository.save(contract);
        return convertToDTO(contract);
    }

    public ContractDTO updateContract(Long id, ContractDTO contractDTO) {
        Optional<Contract> optionalContract = contractRepository.findById(id);
        if (optionalContract.isPresent()) {
            Contract contract = optionalContract.get();
            contract.setContractType(ContractType.valueOf(contractDTO.getContractType()));
            contract.setStartDate(contractDTO.getStartDate());
            contract.setEndDate(contractDTO.getEndDate());
            contract.setRenewalDate(contractDTO.getRenewalDate());
            contract.setReference(contractDTO.getReference());
            contract = contractRepository.save(contract);
            return convertToDTO(contract);
        }
        return null;
    }

    public void deleteContract(Long id) {
        contractRepository.deleteById(id);
    }

    private ContractDTO convertToDTO(Contract contract) {
        ContractDTO dto = new ContractDTO();
        dto.setContractId(contract.getContractId());
        dto.setContractType(String.valueOf(contract.getContractType()));
        dto.setStartDate(contract.getStartDate());
        dto.setEndDate(contract.getEndDate());
        dto.setRenewalDate(contract.getRenewalDate());
        dto.setReference(contract.getReference());
        return dto;
    }

    private Contract convertToEntity(ContractDTO dto) {
        Contract contract = new Contract();
        contract.setContractId(dto.getContractId());
        contract.setContractType(ContractType.valueOf(dto.getContractType()));
        contract.setStartDate(dto.getStartDate());
        contract.setEndDate(dto.getEndDate());
        contract.setRenewalDate(dto.getRenewalDate());
        contract.setReference(dto.getReference());
        return contract;
    }
}
