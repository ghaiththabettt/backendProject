package com.bezkoder.springjwt.HRModuleServices;



import com.bezkoder.springjwt.dtos.HRModuleDtos.AttendanceDTO;
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.HRModuleEntities.Attendance;
import com.bezkoder.springjwt.repository.HRModuleRepository.AttendanceRepository;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<AttendanceDTO> getAllAttendances() {
        return attendanceRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Optional<AttendanceDTO> getAttendanceById(Long id) {
        return attendanceRepository.findById(id).map(this::convertToDTO);
    }

    public AttendanceDTO saveAttendance(AttendanceDTO attendanceDTO) {
        Attendance attendance = convertToEntity(attendanceDTO);
        attendance = attendanceRepository.save(attendance);
        return convertToDTO(attendance);
    }

    public void deleteAttendance(Long id) {
        attendanceRepository.deleteById(id);
    }

    private AttendanceDTO convertToDTO(Attendance attendance) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setAttendanceId(attendance.getAttendanceId());
        dto.setDate(attendance.getDate());
        dto.setStatus(attendance.getCheckIn() != null ? "Present" : "Absent");
        dto.setEmployeeId(attendance.getEmployee().getId());
        return dto;
    }

    private Attendance convertToEntity(AttendanceDTO dto) {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(dto.getAttendanceId());
        attendance.setDate(dto.getDate());

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        attendance.setEmployee(employee);
        return attendance;
    }

    public AttendanceDTO createAttendance(AttendanceDTO attendanceDTO) {

        return attendanceDTO;
    }
}
