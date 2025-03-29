package com.bezkoder.springjwt.controllers.HRModuleControllers;


import com.bezkoder.springjwt.dtos.HRModuleDtos.TimeSheetDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.TimeSheet;
import com.bezkoder.springjwt.HRModuleServices.TimeSheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timesheets")
public class TimeSheetController {

    @Autowired
    private TimeSheetService timeSheetService;

    // ✅ Ajouter une nouvelle feuille de temps
    @PostMapping
    public ResponseEntity<TimeSheet> addTimeSheet(@RequestBody TimeSheetDTO timeSheetDTO) {
        TimeSheet newTimeSheet = timeSheetService.addTimeSheet(timeSheetDTO);
        return ResponseEntity.ok(newTimeSheet);
    }

    // ✅ Mettre à jour une feuille de temps
    @PutMapping("/{id}")
    public ResponseEntity<TimeSheet> updateTimeSheet(@PathVariable Long id, @RequestBody TimeSheetDTO timeSheetDTO) {
        TimeSheet updatedTimeSheet = timeSheetService.updateTimeSheet(id, timeSheetDTO);
        return ResponseEntity.ok(updatedTimeSheet);
    }

    // ✅ Supprimer une feuille de temps
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeSheet(@PathVariable Long id) {
        timeSheetService.deleteTimeSheet(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Récupérer une feuille de temps par son ID
    @GetMapping("/{id}")
    public ResponseEntity<TimeSheet> getTimeSheetById(@PathVariable Long id) {
        TimeSheet timeSheet = timeSheetService.getTimeSheetById(id);
        return ResponseEntity.ok(timeSheet);
    }

    // ✅ Récupérer toutes les feuilles de temps d'un employé
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<TimeSheet>> getAllTimeSheetsForEmployee(@PathVariable Long employeeId) {
        List<TimeSheet> timeSheets = timeSheetService.getAllTimeSheetsForEmployee(employeeId);
        return ResponseEntity.ok(timeSheets);
    }
}
