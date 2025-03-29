package com.bezkoder.springjwt.HRModuleServices;

import com.bezkoder.springjwt.models.HRModuleEntities.JobPosition;
import com.bezkoder.springjwt.dtos.HRModuleDtos.JobPositionDTO;
import com.bezkoder.springjwt.repository.HRModuleRepository.JobPositionRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobPositionService {

    @Autowired
    private JobPositionRepository jobPositionRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Méthode existante pour convertir en DTO
    private JobPositionDTO convertToDto(JobPosition jobPosition) {
        return modelMapper.map(jobPosition, JobPositionDTO.class);
    }

    // Méthode existante pour récupérer tous les postes
    public List<JobPositionDTO> getAllJobPositions() {
        List<JobPosition> jobPositions = jobPositionRepository.findAll();
        return jobPositions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Nouvelle méthode : récupérer les postes par titre
    public List<JobPositionDTO> getJobPositionsByTitle(String title) {
        List<JobPosition> positions = jobPositionRepository.findByTitleContainingIgnoreCase(title);
        return positions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Autres méthodes existantes (create, getById, update, delete)...
    public JobPositionDTO createJobPosition(JobPositionDTO jobPositionDTO) {
        JobPosition jobPosition = modelMapper.map(jobPositionDTO, JobPosition.class);
        JobPosition savedJobPosition = jobPositionRepository.save(jobPosition);
        return convertToDto(savedJobPosition);
    }

    public JobPositionDTO getJobPositionById(Long id) {
        Optional<JobPosition> jobPosition = jobPositionRepository.findById(id);
        return jobPosition.map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("Job Position not found with id " + id));
    }

    public JobPositionDTO updateJobPosition(Long id, JobPositionDTO jobPositionDTO) {
        if (!jobPositionRepository.existsById(id)) {
            throw new RuntimeException("Job Position not found with id " + id);
        }
        JobPosition jobPosition = modelMapper.map(jobPositionDTO, JobPosition.class);
        jobPosition.setJobPositionId(id); // on s'assure que l'ID reste inchangé
        JobPosition updatedJobPosition = jobPositionRepository.save(jobPosition);
        return convertToDto(updatedJobPosition);
    }

    public void deleteJobPosition(Long id) {
        if (!jobPositionRepository.existsById(id)) {
            throw new RuntimeException("Job Position not found with id " + id);
        }
        jobPositionRepository.deleteById(id);
    }
}
