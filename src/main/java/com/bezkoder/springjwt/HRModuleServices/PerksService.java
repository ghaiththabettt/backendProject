package com.bezkoder.springjwt.HRModuleServices;



import com.bezkoder.springjwt.dtos.HRModuleDtos.PerksDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.Perks;
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.repository.HRModuleRepository.PerksRepository;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class  PerksService  implements IPerksService {

    @Autowired
    private PerksRepository perksRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public PerksDTO createPerks(PerksDTO perksDTO) {
        Employee employee = employeeRepository.findById(perksDTO.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Perks perks = new Perks();
        perks.setEmployee(employee);
        perks.setPerksType(perksDTO.getPerksType());
        perks.setDatePerks(perksDTO.getDatePerks());
        perks.setReason(perksDTO.getReason());

        perks = perksRepository.save(perks);

        return mapToDTO(perks);
    }

    @Override
    public PerksDTO updatePerks(Long perksId, PerksDTO perksDTO) {
        Perks existingPerks = perksRepository.findById(perksId)
                .orElseThrow(() -> new RuntimeException("Perks not found"));

        Employee employee = employeeRepository.findById(perksDTO.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        existingPerks.setEmployee(employee);
        existingPerks.setPerksType(perksDTO.getPerksType());
        existingPerks.setDatePerks(perksDTO.getDatePerks());
        existingPerks.setReason(perksDTO.getReason());

        perksRepository.save(existingPerks);

        return mapToDTO(existingPerks);
    }

    @Override
    public void deletePerks(Long perksId) {
        Perks existingPerks = perksRepository.findById(perksId)
                .orElseThrow(() -> new RuntimeException("Perks not found"));

        perksRepository.delete(existingPerks);
    }

    @Override
    public List<PerksDTO> getAllPerks() {
        List<Perks> perksList = perksRepository.findAll();
        return perksList.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PerksDTO getPerksById(Long perksId) {
        Perks perks = perksRepository.findById(perksId)
                .orElseThrow(() -> new RuntimeException("Perks not found"));
        return mapToDTO(perks);
    }

    private PerksDTO mapToDTO(Perks perks) {
        PerksDTO perksDTO = new PerksDTO();
        perksDTO.setPerksId(perks.getPerksId());
        perksDTO.setEmployeeId(perks.getEmployee().getId());  // Assuming Employee has getId() method
        perksDTO.setPerksType(perks.getPerksType());
        perksDTO.setDatePerks(perks.getDatePerks());
        perksDTO.setReason(perks.getReason());
        return perksDTO;
    }
}
