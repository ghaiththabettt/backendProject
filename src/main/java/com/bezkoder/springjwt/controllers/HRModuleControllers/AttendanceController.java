package com.bezkoder.springjwt.controllers.HRModuleControllers;


import com.bezkoder.springjwt.dtos.HRModuleDtos.AttendanceDTO;
import com.bezkoder.springjwt.HRModuleServices.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // Endpoint pour créer une nouvelle présence
    @PostMapping
    public ResponseEntity<AttendanceDTO> createAttendance(@RequestBody AttendanceDTO attendanceDTO) {
        AttendanceDTO createdAttendance = attendanceService.createAttendance(attendanceDTO);
        return new ResponseEntity<>(createdAttendance, HttpStatus.CREATED);
    }

    // Endpoint pour obtenir toutes les présences
    @GetMapping
    public ResponseEntity<List<AttendanceDTO>> getAllAttendances() {
        List<AttendanceDTO> attendances = attendanceService.getAllAttendances();
        return new ResponseEntity<>(attendances, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceDTO> getAttendanceById(@PathVariable("id") Long attendanceId) {
        Optional<AttendanceDTO> attendance = attendanceService.getAttendanceById(attendanceId);

        if (attendance.isPresent()) {
            return new ResponseEntity<>(attendance.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
