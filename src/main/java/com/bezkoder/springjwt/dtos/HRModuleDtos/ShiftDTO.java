package com.bezkoder.springjwt.dtos.HRModuleDtos;

import lombok.Data;

import java.time.LocalTime;

@Data
public class ShiftDTO {
    private Long shiftId;
    private LocalTime startTime;
    private LocalTime endTime;

    private Long employeeId;
}
