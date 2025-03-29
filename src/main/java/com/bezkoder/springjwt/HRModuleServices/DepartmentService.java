package com.bezkoder.springjwt.HRModuleServices;

import com.bezkoder.springjwt.dtos.HRModuleDtos.DepartmentDTO;
import com.bezkoder.springjwt.models.HRModuleEntities.Department;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.DepartmentRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.JobPositionRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.PolicyRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.TrainingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    @Autowired
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final JobPositionRepository jobPositionRepository;
    private final TrainingRepository trainingRepository;
    private final PolicyRepository policyRepository;

    public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository, JobPositionRepository jobPositionRepository, TrainingRepository trainingRepository, PolicyRepository policyRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.jobPositionRepository = jobPositionRepository;
        this.trainingRepository = trainingRepository;
        this.policyRepository = policyRepository;
    }


    public DepartmentDTO createDepartment(DepartmentDTO departmentDTO) {
        Department department = new Department();
        department.setDepartmentName(departmentDTO.getDepartmentName());

        Department savedDepartment = departmentRepository.save(department);
        return convertToDTO(savedDepartment);
    }

    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DepartmentDTO getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public DepartmentDTO updateDepartment(Long id, DepartmentDTO departmentDTO) {
        return departmentRepository.findById(id).map(department -> {
            department.setDepartmentName(departmentDTO.getDepartmentName());
            Department updatedDepartment = departmentRepository.save(department);
            return convertToDTO(updatedDepartment);
        }).orElse(null);
    }

    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }

    private DepartmentDTO convertToDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setDepartmentId(department.getDepartmentId());
        dto.setDepartmentName(department.getDepartmentName());
        return dto;
    }
}
