package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TimeSheetDTO {
    private Long timeSheetId;
    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;
    private Double hoursWorked;

    private Long employeeId;

    public String getTaskDescription() {
        return null;
    }
}