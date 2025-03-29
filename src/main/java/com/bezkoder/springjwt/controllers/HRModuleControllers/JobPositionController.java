package com.bezkoder.springjwt.controllers.HRModuleControllers;

import com.bezkoder.springjwt.dtos.HRModuleDtos.JobPositionDTO;
import com.bezkoder.springjwt.HRModuleServices.JobPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-positions")
@CrossOrigin(origins = "*")
public class JobPositionController {

    @Autowired
    private JobPositionService jobPositionService;

    @PostMapping
    public ResponseEntity<JobPositionDTO> createJobPosition(@RequestBody JobPositionDTO jobPositionDTO) {
        JobPositionDTO createdJobPosition = jobPositionService.createJobPosition(jobPositionDTO);
        return ResponseEntity.ok(createdJobPosition);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPositionDTO> getJobPositionById(@PathVariable Long id) {
        JobPositionDTO jobPosition = jobPositionService.getJobPositionById(id);
        return ResponseEntity.ok(jobPosition);
    }

    @GetMapping("/allPosition")
    public ResponseEntity<List<JobPositionDTO>> getAllJobPositions() {
        List<JobPositionDTO> jobPositions = jobPositionService.getAllJobPositions();
        return ResponseEntity.ok(jobPositions);
    }

    // Nouvel endpoint pour rechercher par titre
    @GetMapping("/by-title")
    public ResponseEntity<List<JobPositionDTO>> getJobPositionsByTitle(@RequestParam String title) {
        List<JobPositionDTO> positions = jobPositionService.getJobPositionsByTitle(title);
        return ResponseEntity.ok(positions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobPositionDTO> updateJobPosition(@PathVariable Long id, @RequestBody JobPositionDTO jobPositionDTO) {
        JobPositionDTO updatedJobPosition = jobPositionService.updateJobPosition(id, jobPositionDTO);
        return ResponseEntity.ok(updatedJobPosition);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJobPosition(@PathVariable Long id) {
        jobPositionService.deleteJobPosition(id);
        return ResponseEntity.ok("Job Position with ID " + id + " has been deleted successfully.");
    }
}
