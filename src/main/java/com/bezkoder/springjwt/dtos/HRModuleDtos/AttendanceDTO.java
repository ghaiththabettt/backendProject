package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.*;

import java.time.LocalDate;
@Getter
@Setter
@Data
public class AttendanceDTO {
    private Long attendanceId;
    private LocalDate date;
    private String status; // "Present", "Absent", etc.

    private Long employeeId;

}
