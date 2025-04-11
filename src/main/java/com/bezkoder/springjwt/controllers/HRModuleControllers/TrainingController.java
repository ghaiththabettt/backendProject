package com.bezkoder.springjwt.controllers.HRModuleControllers;


import com.bezkoder.springjwt.HRModuleServices.EmployeeService;
import com.bezkoder.springjwt.dtos.HRModuleDtos.TrainingDTO;
import com.bezkoder.springjwt.HRModuleServices.TrainingService;
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.HRModuleEntities.Training;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    @Autowired
    private TrainingService trainingService;
    private EmployeeService employeeService;

    // Ajouter une formation
    @PostMapping
    public ResponseEntity<TrainingDTO> addTraining(@RequestBody TrainingDTO trainingDTO) {
        TrainingDTO createdTraining = trainingService.addTraining(trainingDTO);
        return new ResponseEntity<>(createdTraining, HttpStatus.CREATED);
    }

    @GetMapping("/department/{departmentId}")

    public List<Training> getTrainingsByDepartmentId(@PathVariable Long departmentId) {
        return trainingService.getTrainingsByDepartmentId(departmentId);
    }
    // Mettre à jour une formation
    @PutMapping("/{id}")
    public ResponseEntity<TrainingDTO> updateTraining(@PathVariable Long id, @RequestBody TrainingDTO trainingDTO) {
        try {
            TrainingDTO updatedTraining = trainingService.updateTraining(id, trainingDTO);
            return new ResponseEntity<>(updatedTraining, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Récupérer toutes les formations
    @GetMapping
    public ResponseEntity<List<TrainingDTO>> getAllTrainings() {
        List<TrainingDTO> trainings = trainingService.getAllTrainings();
        return new ResponseEntity<>(trainings, HttpStatus.OK);
    }

    // Récupérer une formation par son ID
    @GetMapping("/{id}")
    public ResponseEntity<TrainingDTO> getTrainingById(@PathVariable Long id) {
        try {
            TrainingDTO training = trainingService.getTrainingById(id);
            return new ResponseEntity<>(training, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Supprimer une formation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(@PathVariable Long id) {
        try {
            trainingService.deleteTraining(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<Employee>> getParticipantsForTraining(@PathVariable Long id) {
        try {
            List<Employee> participants = trainingService.getParticipantsByTrainingId(id);
            return new ResponseEntity<>(participants, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/with-participants")
    public ResponseEntity<List<Map<String, Object>>> getAllTrainingsWithParticipants() {
        try {
            List<Map<String, Object>> data = trainingService.getAllTrainingsWithParticipants();
            return new ResponseEntity<>(data, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update-training")
    public ResponseEntity<String> updateTraining(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        Long trainingId = Long.valueOf(payload.get("trainingId").toString());
        employeeService.assignEmployeeToTraining(email, trainingId);
        return ResponseEntity.ok("Training updated successfully");
    }

    @PutMapping("/remove-training")
    public ResponseEntity<String> removeTraining(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        employeeService.removeEmployeeFromTraining(email);
        return ResponseEntity.ok("Training removed successfully");
    }


}
