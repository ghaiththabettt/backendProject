package com.bezkoder.springjwt.HRModuleServices;


import com.bezkoder.springjwt.dtos.HRModuleDtos.LeaveDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.DurationType;
import com.bezkoder.springjwt.models.HRModuleEntities.Leave;
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.HRModuleEntities.LeaveType;
import com.bezkoder.springjwt.models.HRModuleEntities.StatusLeave;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.repository.HRModuleRepository.LeaveRepository;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import com.bezkoder.springjwt.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private EmployeeRepository employeeRepository; // Autowire Employee Repository

    @Autowired
    private UserRepository userRepository; // Autowire User Repository

    // --- Conversion Helpers ---

    private Leave convertToEntity(LeaveDTO dto, Leave existingLeave) {
        Leave leave = (existingLeave != null) ? existingLeave : new Leave();

        // Find the employee - crucial for linking the request
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + dto.getEmployeeId()));
        leave.setEmployee(employee);

        // Map fields from DTO, allowing updates only for certain fields or statuses
        leave.setLeaveType(LeaveType.valueOf(dto.getLeaveType())); // Convert string to enum
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setDurationType(DurationType.valueOf(dto.getDurationType()));
        leave.setNumberOfDays(dto.getNumberOfDays()); // Allow setting directly, or recalculate in @PrePersist/@PreUpdate
        leave.setReason(dto.getReason());
        leave.setNote(dto.getNote());

        // Fields usually set by system/actions, not directly from add/update DTO
        if (leave.getLeaveId() == null) { // Only on creation
            leave.setStatusLeave(StatusLeave.PENDING);
            leave.setRequestedOn(LocalDate.now());
            leave.setActionedBy(null);
            leave.setActionDate(null);
        }

        // Optional: recalculate days based on dates/duration if needed
        // leave.calculateDays();

        return leave;
    }

    private LeaveDTO convertToDTO(Leave leave) {
        LeaveDTO dto = new LeaveDTO();
        dto.setLeaveId(leave.getLeaveId());
        dto.setLeaveType(leave.getLeaveType().name());
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setNumberOfDays(leave.getNumberOfDays());
        dto.setDurationType(leave.getDurationType().name());
        dto.setStatusLeave(leave.getStatusLeave().name());
        dto.setReason(leave.getReason());
        dto.setNote(leave.getNote());
        dto.setRequestedOn(leave.getRequestedOn());
        dto.setActionDate(leave.getActionDate());

        if (leave.getEmployee() != null) {
            dto.setEmployeeId(leave.getEmployee().getId());
            dto.setEmployeeName(leave.getEmployee().getName() + " " + leave.getEmployee().getLastName());
            if (leave.getEmployee().getDepartment() != null) {
                dto.setDepartmentName(leave.getEmployee().getDepartment().getDepartmentName());
            } else {
                dto.setDepartmentName("N/A");
            }
            dto.setEmployeeImg(null);
        } else {
            System.err.println("Warning: Employee is null for Leave ID: " + leave.getLeaveId());
        }

        System.out.println("--- Converting Leave ID: " + leave.getLeaveId() + " ---");
        System.out.println("Raw Action Date from Entity: " + leave.getActionDate());
        User actioner = leave.getActionedBy();
        if (actioner != null) {
            System.out.println("Actioned By User Object: PRESENT (ID: " + actioner.getId() + ")");
            try {
                String firstName = actioner.getName();
                String lastName = actioner.getLastName();
                System.out.println("Actioned By User Name: " + firstName);
                System.out.println("Actioned By User Last Name: " + lastName);
                if (firstName == null || lastName == null) {
                    System.err.println("Warning: User (ID: " + actioner.getId() + ") has null name/lastName.");
                    dto.setActionedByName("[Name Unavailable]");
                } else {
                    dto.setActionedByName(firstName + " " + lastName);
                }
                dto.setActionedById(actioner.getId());
            } catch (Exception e) { // Attrape toute exception ici pour le log
                System.err.println("Error accessing ActionedBy User details for Leave ID: " + leave.getLeaveId());
                e.printStackTrace();
                dto.setActionedById(null);
                dto.setActionedByName("[Access Error]");
            }
        } else {
            System.out.println("Actioned By User Object: NULL");
            dto.setActionedById(null);
            dto.setActionedByName(null);
        }
        System.out.println("DTO ActionDate assigned: " + dto.getActionDate());
        System.out.println("--- End Conversion for Leave ID: " + leave.getLeaveId() + " ---");

        return dto;
    }

    // --- CRUD and Action Methods ---

    @Transactional
    public LeaveDTO addLeave(LeaveDTO leaveDTO) {
        // Ensure employeeId is provided
        if (leaveDTO.getEmployeeId() == null) {
            throw new IllegalArgumentException("Employee ID is required to create a leave request.");
        }
        Leave leave = convertToEntity(leaveDTO, null); // Create new entity
        Leave savedLeave = leaveRepository.save(leave);
        return convertToDTO(savedLeave);
    }

    public List<LeaveDTO> getAllLeaves() {
        // Utilise la nouvelle méthode du repository avec JOIN FETCH
        return leaveRepository.findAllWithDetails()
                .stream()
                .map(this::convertToDTO) // Maintenant, actionedBy et employee devraient être chargés
                .collect(Collectors.toList());
    }

    // *** MODIFICATION ICI ***
    public LeaveDTO getLeaveById(Long leaveId) {
        // Utilise la nouvelle méthode du repository avec JOIN FETCH
        Leave leave = leaveRepository.findByIdWithDetails(leaveId)
                .orElseThrow(() -> new EntityNotFoundException("Leave not found with ID: " + leaveId));
        // actionedBy et employee devraient être chargés grâce au JOIN FETCH
        return convertToDTO(leave);
    }
    @Transactional
    public LeaveDTO updateLeave(Long leaveId, LeaveDTO leaveDTO) {
        Leave existingLeave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new EntityNotFoundException("Leave not found with ID: " + leaveId));

        // Business Rule: Only allow updates if the status is PENDING
        if (existingLeave.getStatusLeave() != StatusLeave.PENDING) {
            throw new IllegalStateException("Cannot update a leave request that is already " + existingLeave.getStatusLeave());
        }

        // Ensure employee cannot be changed during update
        if (!existingLeave.getEmployee().getId().equals(leaveDTO.getEmployeeId())) {
            throw new IllegalArgumentException("Cannot change the employee associated with the leave request.");
        }

        // Update allowed fields using the converter
        Leave updatedLeaveData = convertToEntity(leaveDTO, existingLeave);

        Leave savedLeave = leaveRepository.save(updatedLeaveData);
        return convertToDTO(savedLeave);
    }

    @Transactional
    public void deleteLeave(Long leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new EntityNotFoundException("Leave not found with ID: " + leaveId));

        // Business Rule: Optional - prevent deletion of approved/rejected leaves?
        // if (leave.getStatusLeave() != StatusLeave.PENDING) {
        //     throw new IllegalStateException("Cannot delete a leave request that is already " + leave.getStatusLeave());
        // }

        leaveRepository.deleteById(leaveId);
    }

    @Transactional
    public LeaveDTO approveLeave(Long leaveId, Long actionUserId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new EntityNotFoundException("Leave not found with ID: " + leaveId));
        User actionUser = userRepository.findById(actionUserId)
                .orElseThrow(() -> new EntityNotFoundException("User performing action not found with ID: " + actionUserId));

        if (leave.getStatusLeave() != StatusLeave.PENDING) {
            throw new IllegalStateException("Leave request is not in PENDING status. Current status: " + leave.getStatusLeave());
        }

        leave.setStatusLeave(StatusLeave.APPROVED);
        leave.setActionedBy(actionUser);
        leave.setActionDate(LocalDate.now());
        Leave savedLeave = leaveRepository.save(leave);
        return convertToDTO(savedLeave);
    }

    @Transactional
    public LeaveDTO rejectLeave(Long leaveId, Long actionUserId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new EntityNotFoundException("Leave not found with ID: " + leaveId));
        User actionUser = userRepository.findById(actionUserId)
                .orElseThrow(() -> new EntityNotFoundException("User performing action not found with ID: " + actionUserId));

        if (leave.getStatusLeave() != StatusLeave.PENDING) {
            throw new IllegalStateException("Leave request is not in PENDING status. Current status: " + leave.getStatusLeave());
        }

        leave.setStatusLeave(StatusLeave.REJECTED);
        leave.setActionedBy(actionUser); // Store who rejected it
        leave.setActionDate(LocalDate.now());
        Leave savedLeave = leaveRepository.save(leave);
        return convertToDTO(savedLeave);
    }
}