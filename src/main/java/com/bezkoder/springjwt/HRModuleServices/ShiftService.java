package com.bezkoder.springjwt.HRModuleServices;

import com.bezkoder.springjwt.dtos.HRModuleDtos.ShiftDTO;
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.HRModuleEntities.Shift;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.ShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShiftService {

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public ShiftDTO createShift(ShiftDTO shiftDTO) {
        Optional<Employee> employeeOpt = employeeRepository.findById(shiftDTO.getEmployeeId());
        if (employeeOpt.isEmpty()) {
            throw new RuntimeException("Employee not found");
        }

        Shift shift = new Shift();
        shift.setShiftName("Shift"); // Remplacer par un champ dans ShiftDTO si nécessaire
        shift.setStartTime(shiftDTO.getStartTime());
        shift.setEndTime(shiftDTO.getEndTime());
        shift.setEmployee(employeeOpt.get());

        shift = shiftRepository.save(shift);

        shiftDTO.setShiftId(shift.getShiftId());
        return shiftDTO;
    }

    public List<ShiftDTO> getAllShifts() {
        List<Shift> shifts = shiftRepository.findAll();
        return shifts.stream().map(shift -> {
            ShiftDTO dto = new ShiftDTO();
            dto.setShiftId(shift.getShiftId());
            dto.setStartTime(shift.getStartTime());
            dto.setEndTime(shift.getEndTime());
            dto.setEmployeeId(shift.getEmployee().getId());
            return dto;
        }).collect(Collectors.toList());
    }

    public ShiftDTO getShiftById(Long shiftId) {
        Optional<Shift> shiftOpt = shiftRepository.findById(shiftId);
        if (shiftOpt.isEmpty()) {
            throw new RuntimeException("Shift not found");
        }

        Shift shift = shiftOpt.get();
        ShiftDTO dto = new ShiftDTO();
        dto.setShiftId(shift.getShiftId());
        dto.setStartTime(shift.getStartTime());
        dto.setEndTime(shift.getEndTime());
        dto.setEmployeeId(shift.getEmployee().getId());

        return dto;
    }

    public ShiftDTO updateShift(Long shiftId, ShiftDTO shiftDTO) {
        Optional<Shift> shiftOpt = shiftRepository.findById(shiftId);
        if (shiftOpt.isEmpty()) {
            throw new RuntimeException("Shift not found");
        }

        Shift shift = shiftOpt.get();
        shift.setStartTime(shiftDTO.getStartTime());
        shift.setEndTime(shiftDTO.getEndTime());

        shift = shiftRepository.save(shift);

        shiftDTO.setShiftId(shift.getShiftId());
        return shiftDTO;
    }

    public void deleteShift(Long shiftId) {
        shiftRepository.deleteById(shiftId);
    }

    public ShiftDTO saveShift(ShiftDTO shiftDTO) {
        return shiftDTO;
    }
}