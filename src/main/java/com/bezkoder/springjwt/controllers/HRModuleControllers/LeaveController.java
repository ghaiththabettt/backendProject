package com.bezkoder.springjwt.controllers.HRModuleControllers;


import com.bezkoder.springjwt.dtos.HRModuleDtos.LeaveDTO;
import com.bezkoder.springjwt.HRModuleServices.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    // Ajouter un congé
    @PostMapping
    public ResponseEntity<LeaveDTO> addLeave(@RequestBody LeaveDTO leaveDTO) {
        LeaveDTO createdLeave = leaveService.addLeave(leaveDTO);
        return new ResponseEntity<>(createdLeave, HttpStatus.CREATED);
    }

    // Récupérer tous les congés
    @GetMapping
    public ResponseEntity<List<LeaveDTO>> getAllLeaves() {
        List<LeaveDTO> leaves = leaveService.getAllLeaves();
        return new ResponseEntity<>(leaves, HttpStatus.OK);
    }

    // Récupérer un congé par son ID
    @GetMapping("/{leaveId}")
    public ResponseEntity<LeaveDTO> getLeaveById(@PathVariable Long leaveId) {
        LeaveDTO leave = leaveService.getLeaveById(leaveId);
        return new ResponseEntity<>(leave, HttpStatus.OK);
    }

    // Mettre à jour un congé
    @PutMapping("/{leaveId}")
    public ResponseEntity<LeaveDTO> updateLeave(@PathVariable Long leaveId, @RequestBody LeaveDTO leaveDTO) {
        LeaveDTO updatedLeave = leaveService.updateLeave(leaveId, leaveDTO);
        return new ResponseEntity<>(updatedLeave, HttpStatus.OK);
    }

    // Supprimer un congé
    @DeleteMapping("/{leaveId}")
    public ResponseEntity<Void> deleteLeave(@PathVariable Long leaveId) {
        leaveService.deleteLeave(leaveId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
