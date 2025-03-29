package com.bezkoder.springjwt.HRModuleServices;



import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.HRModuleEntities.Department;
import com.bezkoder.springjwt.models.HRModuleEntities.Training;
import com.bezkoder.springjwt.dtos.HRModuleDtos.TrainingDTO;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.DepartmentRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.TrainingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrainingService {

    @Autowired
    private TrainingRepository trainingRepository;
    private DepartmentRepository departmentRepository;
    private EmployeeRepository employeeRepository;

    // Convertir Training en TrainingDTO
    private TrainingDTO convertToDTO(Training training) {
        TrainingDTO dto = new TrainingDTO();
        dto.setTrainingId(training.getTrainingId());
        dto.setTopic(training.getTrainingName());
        dto.setStartDate(training.getStartDate());
        dto.setEndDate(training.getEndDate());
        dto.setTrainingType(training.getTrainingType());

        // Si vous avez une liste de participants, vous pouvez la remplir ici
        // dto.setParticipantIds(training.getParticipants().stream().map(Employee::getId).collect(Collectors.toList()));
        return dto;
    }

    // Convertir TrainingDTO en Training
    private Training convertToEntity(TrainingDTO dto) {
        Training training = new Training();
        training.setTrainingId(dto.getTrainingId());
        training.setTrainingName(dto.getTopic());
        training.setStartDate(dto.getStartDate());
        training.setEndDate(dto.getEndDate());
        training.setTrainingType(dto.getTrainingType());

        // Ajoutez ici la logique pour lier les employés si nécessaire
        return training;
    }

    // Ajouter une formation
    public TrainingDTO addTraining(TrainingDTO trainingDTO) {
        Training training = convertToEntity(trainingDTO);
        Training savedTraining = trainingRepository.save(training);
        return convertToDTO(savedTraining);
    }

    // Mettre à jour une formation
    public TrainingDTO updateTraining(Long id, TrainingDTO trainingDTO) {
        Optional<Training> existingTraining = trainingRepository.findById(id);
        if (existingTraining.isPresent()) {
            Training training = existingTraining.get();
            training.setTrainingName(trainingDTO.getTopic());
            training.setStartDate(trainingDTO.getStartDate());
            training.setEndDate(trainingDTO.getEndDate());
            training.setTrainingType(trainingDTO.getTrainingType()); // ✅ ajout
            Training updatedTraining = trainingRepository.save(training);
            return convertToDTO(updatedTraining);
        } else {
            throw new RuntimeException("Formation non trouvée pour l'ID: " + id);
        }
    }

    // Récupérer toutes les formations
    public List<TrainingDTO> getAllTrainings() {
        List<Training> trainings = trainingRepository.findAll();
        return trainings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Récupérer une formation par son ID
    public TrainingDTO getTrainingById(Long id) {
        Optional<Training> training = trainingRepository.findById(id);
        return training.map(this::convertToDTO).orElseThrow(() -> new RuntimeException("Formation non trouvée pour l'ID: " + id));
    }

    // Supprimer une formation
    public void deleteTraining(Long id) {
        Optional<Training> existingTraining = trainingRepository.findById(id);
        if (existingTraining.isPresent()) {
            trainingRepository.delete(existingTraining.get());
        } else {
            throw new RuntimeException("Formation non trouvée pour l'ID: " + id);
        }
    }

    public List<Employee> getParticipantsByTrainingId(Long trainingId) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new RuntimeException("Training not found"));

        Department department = training.getDepartment();
        return department.getEmployees(); // Tous les employés du même département
    }

    public List<Map<String, Object>> getAllTrainingsWithParticipants() {
        List<Training> trainings = trainingRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Training training : trainings) {
            Department department = training.getDepartment();
            if (department != null && department.getEmployees() != null) {
                for (Employee employee : department.getEmployees()) {
                    Map<String, Object> trainerMap = new HashMap<>();
                    trainerMap.put("name", employee.getName());
                    trainerMap.put("lastName", employee.getLastName());
                    trainerMap.put("email", employee.getEmail());
                    trainerMap.put("departmentName", department.getDepartmentName());
                    trainerMap.put("trainingName", training.getTrainingName());
                    trainerMap.put("trainingType", training.getTrainingType());
                    trainerMap.put("location", training.getLocation());
                    trainerMap.put("startDate", training.getStartDate());
                    trainerMap.put("endDate", training.getEndDate());
                    result.add(trainerMap);
                }
            }
        }

        return result;
    }


}
