package com.bezkoder.springjwt.controllers;


import com.bezkoder.springjwt.dtos.HRModuleDtos.EmployeeDTO;
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.EEmployeePosition;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import com.bezkoder.springjwt.repository.UserRepository;
import com.bezkoder.springjwt.HRModuleServices.EmployeeService;
import com.bezkoder.springjwt.security.services.UserDetailsImpl;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/employee")
public class EmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/position/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<?> getEmployeePosition(@PathVariable Long userId) {
        // First check if the user exists
        Optional<User> userOptional = userRepository.findById(userId);

        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: User not found!"));
        }

        // Then check if the user is an employee
        User user = userOptional.get();
        if (!user.getUserType().name().equals("ROLE_EMPLOYEE")) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: User is not an employee!"));
        }

        // Now get the employee details
        Optional<Employee> employeeOptional = employeeRepository.findById(userId);

        if (employeeOptional.isPresent()) {
            Employee employee = employeeOptional.get();
            EEmployeePosition position = employee.getPosition();

            Map<String, String> response = new HashMap<>();
            response.put("position", position != null ? position.name() : "");

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Employee details not found!"));
        }
    }

    @GetMapping("/my-position")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<?> getMyPosition() {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        System.out.println("Getting position for user ID: " + userDetails.getId());

        // Get the employee details
        Optional<Employee> employeeOptional = employeeRepository.findById(userDetails.getId());

        if (employeeOptional.isPresent()) {
            Employee employee = employeeOptional.get();
            EEmployeePosition position = employee.getPosition();

            System.out.println("Employee found, position: " + (position != null ? position.name() : "null"));

            Map<String, String> response = new HashMap<>();
            response.put("position", position != null ? position.name() : "");

            return ResponseEntity.ok(response);
        } else {
            System.out.println("Employee not found for user ID: " + userDetails.getId());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Employee details not found!"));
        }
    }

    @PutMapping("/my-position")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<?> updateMyPosition(@RequestBody Map<String, String> positionData) {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        System.out.println("Updating position for user ID: " + userDetails.getId());

        // Get the position from the request
        String positionStr = positionData.get("position");
        if (positionStr == null || positionStr.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Position is required!"));
        }

        System.out.println("New position: " + positionStr);

        // Convert string to enum
        EEmployeePosition position;
        try {
            position = EEmployeePosition.valueOf(positionStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid position!"));
        }

        // Get the employee details
        Optional<Employee> employeeOptional = employeeRepository.findById(userDetails.getId());

        if (employeeOptional.isPresent()) {
            Employee employee = employeeOptional.get();

            // Update the position
            employee.setPosition(position);
            employeeRepository.save(employee);

            System.out.println("Position updated to: " + employee.getPosition());

            Map<String, String> response = new HashMap<>();
            response.put("position", position.name());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Employee details not found!"));
        }
    }
    @GetMapping("/restricted-data")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_HR')")
    public ResponseEntity<?> getRestrictedData() {
        return ResponseEntity.ok("Données visibles seulement par le RH et l'Admin");
    }


    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')  or hasRole('ROLE_HR')")
    public ResponseEntity<?> getAllEmployees() {
        logger.info("Getting all employees with department & contract info");

        List<Employee> employees = employeeRepository.findAll();
        List<Map<String, Object>> employeeList = new ArrayList<>();

        for (Employee employee : employees) {
            Map<String, Object> employeeMap = new HashMap<>();

            // Champs existants
            employeeMap.put("id", employee.getId());
            employeeMap.put("email", employee.getEmail());
            employeeMap.put("position", employee.getPosition() != null
                    ? employee.getPosition().name()
                    : null);

            // Ajout du departmentId (ou null si pas de department)
            if (employee.getDepartment() != null) {
                employeeMap.put("departmentId", employee.getDepartment().getDepartmentId());
            } else {
                employeeMap.put("departmentId", null);
            }

            // Ajout du contractId (ou null)
            // Attention : vous avez un champ "contract" simple + un champ "contracts" (List).
            // Si vous voulez juste le "contract" ManyToOne, on utilise getContract().
            if (employee.getContract() != null) {
                employeeMap.put("contractId", employee.getContract().getContractId());
            } else {
                employeeMap.put("contractId", null);
            }

            // Ajout du name, lastName, dateOfBirth, hireDate, etc.
            employeeMap.put("name", employee.getName());
            employeeMap.put("lastName", employee.getLastName());
            employeeMap.put("dateOfBirth", employee.getDateOfBirth());
            employeeMap.put("hireDate", employee.getHireDate());
            // salary, phoneNumber... (à vous de voir si besoin)

            employeeList.add(employeeMap);
        }

        return ResponseEntity.ok(employeeList);
    }



    @GetMapping("/position-by-email/{email}")
    @PreAuthorize("hasRole('USER') or hasRole('EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<?> getEmployeePositionByEmail(@PathVariable String email) {
        logger.info("Getting position for employee with email: {}", email);

        // Find the employee by email
        Optional<Employee> employeeOptional = employeeRepository.findByEmail(email);

        if (!employeeOptional.isPresent()) {
            logger.warn("Employee not found with email: {}", email);
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Employee not found with email: " + email));
        }

        Employee employee = employeeOptional.get();
        EEmployeePosition position = employee.getPosition();

        logger.info("Found employee, position: {}", position != null ? position.name() : "null");

        Map<String, String> response = new HashMap<>();
        response.put("position", position != null ? position.name() : "");

        return ResponseEntity.ok(response);
    }
    // CREATE (ajouter un employé)
    @PostMapping("/employee")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROLE_HR')")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody Employee employee) {
        try {
            // Vérifie si un rôle est déjà défini, sinon ajoute un rôle par défaut
            if (employee.getPosition() == null) {
                employee.setPosition(EEmployeePosition.PROJECT_MANAGER); // Rôle par défaut si aucun précisé
            }

            Employee savedEmployee = employeeRepository.save(employee);
            return ResponseEntity.ok(savedEmployee);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Unable to create employee"));
        }
    }






    // READ (récupérer un employé par ID)
    @GetMapping("/{id}")
    public EmployeeDTO getEmployeeById(@PathVariable Long id) {
        return employeeService.getEmployeeById(id);
    }

    // UPDATE (mettre à jour un employé)

    @PreAuthorize("hasRole('USER') or hasRole('EMPLOYEE') or hasRole('ADMIN')")

    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable Long id, @RequestBody EmployeeDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authenticated user: " + authentication.getName());  // Loguer l'utilisateur authentifié

        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        // Assurez-vous que l'utilisateur a un rôle approprié
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            System.out.println("User is admin");
        }
        return employeeService.updateEmployee(id, dto);
    }


    // DELETE (supprimer un employé)
    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
    }
}
