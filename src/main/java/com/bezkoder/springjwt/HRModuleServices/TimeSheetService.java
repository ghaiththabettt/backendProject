package com.bezkoder.springjwt.HRModuleServices;


import com.bezkoder.springjwt.dtos.HRModuleDtos.TimeSheetDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.TimeSheet;
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.repository.HRModuleRepository.TimeSheetRepository;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class TimeSheetService {

    @Autowired
    private TimeSheetRepository timeSheetRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    // Ajouter une nouvelle feuille de temps
    public TimeSheet addTimeSheet(TimeSheetDTO timeSheetDTO) {
        Employee employee = employeeRepository.findById(timeSheetDTO.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        TimeSheet timeSheet = new TimeSheet();
        timeSheet.setEmployee(employee);
        timeSheet.setDate(timeSheetDTO.getDate());

        // Calcul des heures travaillées
        LocalTime timeIn = timeSheetDTO.getTimeIn();
        LocalTime timeOut = timeSheetDTO.getTimeOut();
        long hoursWorked = java.time.Duration.between(timeIn, timeOut).toHours();
        timeSheet.setHoursWorked(hoursWorked);

        timeSheet.setTaskDescription(timeSheetDTO.getTaskDescription());

        return timeSheetRepository.save(timeSheet);
    }

    // Mettre à jour une feuille de temps existante
    public TimeSheet updateTimeSheet(Long timesheetId, TimeSheetDTO timeSheetDTO) {
        TimeSheet existingTimeSheet = timeSheetRepository.findById(timesheetId)
                .orElseThrow(() -> new RuntimeException("TimeSheet not found"));

        existingTimeSheet.setDate(timeSheetDTO.getDate());
        existingTimeSheet.setTaskDescription(timeSheetDTO.getTaskDescription());

        LocalTime timeIn = timeSheetDTO.getTimeIn();
        LocalTime timeOut = timeSheetDTO.getTimeOut();
        long hoursWorked = java.time.Duration.between(timeIn, timeOut).toHours();
        existingTimeSheet.setHoursWorked(hoursWorked);

        return timeSheetRepository.save(existingTimeSheet);
    }

    // Supprimer une feuille de temps
    public void deleteTimeSheet(Long timesheetId) {
        TimeSheet timeSheet = timeSheetRepository.findById(timesheetId)
                .orElseThrow(() -> new RuntimeException("TimeSheet not found"));
        timeSheetRepository.delete(timeSheet);
    }

    // Récupérer toutes les feuilles de temps d'un employé
    public List<TimeSheet> getAllTimeSheetsForEmployee(Long employeeId) {
        return timeSheetRepository.findByEmployeeId(employeeId);
    }

    // Récupérer une feuille de temps par son ID
    public TimeSheet getTimeSheetById(Long timesheetId) {
        return timeSheetRepository.findById(timesheetId)
                .orElseThrow(() -> new RuntimeException("TimeSheet not found"));
    }
}
