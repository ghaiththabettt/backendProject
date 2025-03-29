package com.bezkoder.springjwt.HRModuleServices;


import com.bezkoder.springjwt.dtos.HRModuleDtos.LeaveDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.Leave;
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.repository.HRModuleRepository.LeaveRepository;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    // Convertir LeaveDTO en Leave entity
    private Leave convertToEntity(LeaveDTO leaveDTO) {
        Employee employee = employeeRepository.findById(leaveDTO.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        return new Leave(
                leaveDTO.getLeaveId(),
                leaveDTO.getLeaveType(),
                leaveDTO.getStartDate(),
                leaveDTO.getEndDate(),
                leaveDTO.getStatusLeave(),
                employee
        );
    }

    // Convertir Leave entity en LeaveDTO
    private LeaveDTO convertToDTO(Leave leave) {
        LeaveDTO leaveDTO = new LeaveDTO();
        leaveDTO.setLeaveId(leave.getLeaveId());
        leaveDTO.setStatusLeave(leave.getStatusLeave());
        leaveDTO.setStartDate(leave.getStartDate());
        leaveDTO.setEndDate(leave.getEndDate());
        leaveDTO.setLeaveType(leave.getLeaveType());
        leaveDTO.setEmployeeId(leave.getEmployee().getId());
        return leaveDTO;
    }

    // Ajouter un nouveau congé
    public LeaveDTO addLeave(LeaveDTO leaveDTO) {
        Leave leave = convertToEntity(leaveDTO);
        leave = leaveRepository.save(leave);
        return convertToDTO(leave);
    }

    // Récupérer tous les congés
    public List<LeaveDTO> getAllLeaves() {
        List<Leave> leaves = leaveRepository.findAll();
        return leaves.stream().map(this::convertToDTO).toList();
    }

    // Récupérer un congé par son ID
    public LeaveDTO getLeaveById(Long leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        return convertToDTO(leave);
    }

    // Mettre à jour un congé
    public LeaveDTO updateLeave(Long leaveId, LeaveDTO leaveDTO) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        leave.setLeaveType(leaveDTO.getLeaveType());
        leave.setStartDate(leaveDTO.getStartDate());
        leave.setEndDate(leaveDTO.getEndDate());
        leave.setStatusLeave(leaveDTO.getStatusLeave());

        leave = leaveRepository.save(leave);
        return convertToDTO(leave);
    }

    // Supprimer un congé
    public void deleteLeave(Long leaveId) {
        leaveRepository.deleteById(leaveId);
    }
}
