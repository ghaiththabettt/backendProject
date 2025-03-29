package com.bezkoder.springjwt.dtos.HRModuleDtos;

import com.bezkoder.springjwt.models.HRModuleEntities.LeaveType;
import com.bezkoder.springjwt.models.HRModuleEntities.StatusLeave;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveDTO {
    private Long leaveId;
    private StatusLeave statusLeave;   // "Pending", "Approved", "Rejected"
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType leaveType;  // "Annual", "Sick", etc.

    private Long employeeId;
}
