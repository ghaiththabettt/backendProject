package com.bezkoder.springjwt.HRModuleServices;

import com.bezkoder.springjwt.dtos.HRModuleDtos.EmployeeDTO;
import com.bezkoder.springjwt.models.EEmployeePosition;
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.HRModuleEntities.Attendance;
import com.bezkoder.springjwt.models.HRModuleEntities.Contract;
import com.bezkoder.springjwt.models.HRModuleEntities.Department;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.ContractRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.DepartmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private final ContractRepository contractRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(ContractRepository contractRepository, EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.contractRepository = contractRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<EmployeeDTO> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        return employee.map(this::convertToDTO).orElse(null);
    }

    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        Employee employee = convertToEntity(employeeDTO);
        employee = employeeRepository.save(employee);
        return convertToDTO(employee);
    }

    @Transactional
    public Employee updateEmployee(Long id, EmployeeDTO dto) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        // Mise à jour des informations de l'employé
        employee.setName(dto.getName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setSalary(dto.getSalary());
        employee.setHireDate(dto.getHireDate());
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setPosition(EEmployeePosition.valueOf(dto.getPosition()));

        // Mise à jour des nouveaux champs
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setAddress(dto.getAddress());

        // Mise à jour du département
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId()).orElse(null);
            employee.setDepartment(department);
        }

        // Mise à jour du contrat
        if (dto.getContractId() != null) {
            Contract contract = contractRepository.findById(dto.getContractId()).orElse(null);
            employee.setContract(contract);
        }

        return employeeRepository.save(employee);
    }




    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        return new EmployeeDTO(
                employee.getId(),
                employee.getName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getHireDate(),
                employee.getSalary(),
                employee.getDateOfBirth(),
                employee.getPosition() != null ? employee.getPosition().name() : null,
                employee.getDepartment() != null ? employee.getDepartment().getId() : null,
                employee.getContract() != null ? employee.getContract().getId() : null,
                employee.getPhoneNumber(),  // Ajout de phoneNumber
                employee.getAddress()       // Ajout de address
        );
    }


    private Employee convertToEntity(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        employee.setName(employeeDTO.getName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setEmail(employeeDTO.getEmail());
        employee.setHireDate(employeeDTO.getHireDate());
        employee.setSalary(employeeDTO.getSalary());
        employee.setDateOfBirth(employeeDTO.getDateOfBirth());
        employee.setPosition(employeeDTO.getPosition() != null
                ? EEmployeePosition.valueOf(employeeDTO.getPosition())
                : null);

        // Ajout de phoneNumber et address
        employee.setPhoneNumber(employeeDTO.getPhoneNumber()); // Ajout du numéro de téléphone
        employee.setAddress(employeeDTO.getAddress());         // Ajout de l'adresse

        if (employeeDTO.getDepartmentId() != null) {
            employee.setDepartment(
                    departmentRepository.findById(employeeDTO.getDepartmentId())
                            .orElse(null)
            );
        }

        if (employeeDTO.getContractId() != null) {
            employee.setContract(
                    contractRepository.findById(employeeDTO.getContractId())
                            .orElse(null)
            );
        }
        return employee;
    }


}

