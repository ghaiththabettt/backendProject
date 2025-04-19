package com.bezkoder.springjwt.dtos.HRModuleDtos;

import com.bezkoder.springjwt.models.HRModuleEntities.DurationType;
import com.bezkoder.springjwt.models.HRModuleEntities.LeaveType;
import com.bezkoder.springjwt.models.HRModuleEntities.StatusLeave;
import lombok.Data;

import java.time.LocalDate;

@Data // Lombok annotation for getters, setters, toString, etc.
public class LeaveDTO {
    private Long leaveId;           // Corresponds to 'id' in Angular Model
    private Long employeeId;        // ID of the employee
    private String employeeName;    // Derived: Employee's full name
    private String employeeImg;     // Derived: URL/path to employee image (optional)
    private String departmentName;  // Derived: Employee's department name

    private String leaveType;       // Enum as String (e.g., "SICK", "ANNUAL")
    private LocalDate startDate;    // Date as LocalDate ("yyyy-MM-dd")
    private LocalDate endDate;      // Date as LocalDate ("yyyy-MM-dd")
    private Double numberOfDays;    // Double for half days
    private String durationType;    // Enum as String (e.g., "FULL_DAY")
    private String statusLeave;     // Enum as String (e.g., "PENDING")
    private String reason;
    private String note;
    private LocalDate requestedOn;  // Date as LocalDate ("yyyy-MM-dd")

    private Long actionedById;      // ID of the user who approved/rejected (was approvedById)
    private String actionedByName;  // Name of the user who approved/rejected (was approvedByName)
    private LocalDate actionDate;   // Date of approval/rejection (was approvalDate)
}
