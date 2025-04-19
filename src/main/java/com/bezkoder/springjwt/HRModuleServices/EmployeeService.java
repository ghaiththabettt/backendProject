package com.bezkoder.springjwt.HRModuleServices;

import com.bezkoder.springjwt.dtos.HRModuleDtos.EmployeeDTO;
import com.bezkoder.springjwt.dtos.HRModuleDtos.EmployeeListDTO;
import com.bezkoder.springjwt.models.EEmployeePosition;
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.HRModuleEntities.Attendance;
import com.bezkoder.springjwt.models.HRModuleEntities.Contract;
import com.bezkoder.springjwt.models.HRModuleEntities.Department;
import com.bezkoder.springjwt.models.HRModuleEntities.Training;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.ContractRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.DepartmentRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.TrainingRepository;
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
    private final TrainingRepository trainingRepository;

    public EmployeeService(ContractRepository contractRepository, EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, TrainingRepository trainingRepository) {
        this.contractRepository = contractRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.trainingRepository = trainingRepository;
    }

    public List<EmployeeDTO> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public EmployeeDTO getEmployeeById(Long id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        return employee.map(this::convertToDTO).orElse(null);
    }

    public List<EmployeeListDTO> getEmployeeList() {
        return employeeRepository.findEmployeeList();
        // Ou si vous récupérez les entités complètes :
        // return employeeRepository.findAllByOrderByNameAscLastNameAsc().stream()
        //      .map(emp -> new EmployeeListDTO(emp.getId(), emp.getName() + " " + emp.getLastName()))
        //      .collect(Collectors.toList());
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
                employee.getDepartment() != null ? employee.getDepartment().getDepartmentId() : null, // ✅ ICI
                employee.getContract() != null ? employee.getContract().getContractId() : null,
                employee.getPhoneNumber(),
                employee.getAddress()
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

    public void removeEmployeeFromTraining(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Même chose ici, logique custom car pas de lien direct
        System.out.println("Training removed from employee: " + employee.getEmail());

        // Si tu as une table pivot ou une logique dans le front pour gérer ça, c’est ici que tu la mets
    }

    public void assignEmployeeToTraining(String email, Long trainingId) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new RuntimeException("Training not found"));

        Department trainingDept = training.getDepartment();
        Department employeeDept = employee.getDepartment();

        if (employeeDept == null || !employeeDept.getDepartmentId().equals(trainingDept.getDepartmentId())) {
            throw new RuntimeException("Employee does not belong to the same department as the training");
        }

        // Ici, comme tu n’as pas de relation directe, tu peux ajouter une logique métier, par exemple :
        // sauvegarder l’association dans une autre table, ou ajouter à une liste custom.
        // Pour l’instant, on log ou fait une action fictive
        System.out.println("Employee " + email + " assigned to training " + training.getTrainingName());
    }
}

