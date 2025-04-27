package com.bezkoder.springjwt.controllers;

import com.bezkoder.springjwt.dtos.HRModuleDtos.EmployeeDTO;
import com.bezkoder.springjwt.dtos.HRModuleDtos.EmployeeListDTO;
import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.EEmployeePosition;
import com.bezkoder.springjwt.models.HRModuleEntities.Contract;
import com.bezkoder.springjwt.models.HRModuleEntities.Department;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    PasswordEncoder encoder;
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }


    @GetMapping("/position/{userId}")
    //@PreAuthorize("hasRole('USER') or hasRole('EMPLOYEE') or hasRole('ADMIN')")
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
    //@PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN')")
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
//@PreAuthorize("hasRole('ADMIN')  or hasRole('ROLE_HR')") // Add @PreAuthorize based on your security needs
    public ResponseEntity<List<Map<String, Object>>> getAllEmployees() { // Explicitly type the return
        logger.info("Getting all employees with department & contract info");

        List<Employee> employees = employeeRepository.findAll(); // Fetch all Employee entities
        List<Map<String, Object>> employeeList = new ArrayList<>();

        for (Employee employee : employees) {
            Map<String, Object> employeeMap = new HashMap<>();

            // Basic Employee fields from the User part and Employee part
            employeeMap.put("id", employee.getId()); // Inherited from User
            employeeMap.put("name", employee.getName()); // Inherited from User
            employeeMap.put("lastName", employee.getLastName()); // Inherited from User
            employeeMap.put("email", employee.getEmail()); // Inherited from User
            employeeMap.put("position", employee.getPosition() != null
                    ? employee.getPosition().name()
                    : null); // From Employee
            employeeMap.put("salary", employee.getSalary()); // From Employee
            employeeMap.put("hireDate", employee.getHireDate()); // From Employee (LocalDate)
            employeeMap.put("dateOfBirth", employee.getDateOfBirth()); // From Employee (Date)
            employeeMap.put("address", employee.getAddress()); // Inherited or Employee field
            employeeMap.put("phoneNumber", employee.getPhoneNumber()); // Inherited or Employee field


            // Add Department information (using the ManyToOne Department relationship)
            if (employee.getDepartment() != null) {
                employeeMap.put("departmentId", employee.getDepartment().getDepartmentId());
                employeeMap.put("departmentName", employee.getDepartment().getDepartmentName()); // Assuming Department has a name field
            } else {
                employeeMap.put("departmentId", null);
                employeeMap.put("departmentName", null);
            }

            // *** Add Contract information (using the OneToMany contracts list) ***
            // We'll find the latest contract based on startDate for demonstration
            if (employee.getContracts() != null && !employee.getContracts().isEmpty()) {
                // Find the contract with the latest start date
                Optional<Contract> latestContractOptional = employee.getContracts().stream()
                        .sorted(Comparator.comparing(Contract::getStartDate).reversed()) // Sort descending by start date
                        .findFirst(); // Get the first element (latest after sorting)

                if (latestContractOptional.isPresent()) {
                    Contract latestContract = latestContractOptional.get();
                    employeeMap.put("latestContractId", latestContract.getContractId());
                    employeeMap.put("latestContractType", latestContract.getContractType() != null ? latestContract.getContractType().name() : null); // Assuming ContractType is an enum
                    employeeMap.put("latestContractStartDate", latestContract.getStartDate()); // LocalDate
                    employeeMap.put("latestContractEndDate", latestContract.getEndDate()); // LocalDate (can be null)
                    employeeMap.put("latestContractReference", latestContract.getReference()); // String
                    employeeMap.put("latestContractStatus", latestContract.getStatut() != null ? latestContract.getStatut().name() : null); // Assuming ContractStatus is an enum
                } else {
                    // This else might not be strictly necessary if the outer check is !isEmpty()
                    // but good for clarity if sorting/finding fails unexpectedly
                    employeeMap.put("latestContractId", null);
                    employeeMap.put("latestContractType", null);
                    employeeMap.put("latestContractStartDate", null);
                    employeeMap.put("latestContractEndDate", null);
                    employeeMap.put("latestContractReference", null);
                    employeeMap.put("latestContractStatus", null);
                }

                // You could also add a count of contracts
                employeeMap.put("contractCount", employee.getContracts().size());

            } else {
                // Employee has no contracts
                employeeMap.put("latestContractId", null);
                employeeMap.put("latestContractType", null);
                employeeMap.put("latestContractStartDate", null);
                employeeMap.put("latestContractEndDate", null);
                employeeMap.put("latestContractReference", null);
                employeeMap.put("latestContractStatus", null);
                employeeMap.put("contractCount", 0);
            }
            // *** End of Contract information logic ***


            employeeList.add(employeeMap);
        }

        return ResponseEntity.ok(employeeList); // Return the list of maps
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

    // @PreAuthorize("hasRole('USER') or hasRole('EMPLOYEE') or hasRole('ADMIN')")

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

    // 🔹 Récupérer les employés par département
    /*@GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROLE_HR')")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByDepartment(@PathVariable Long departmentId) {
        List<EmployeeDTO> employees = employeeService.getAllEmployees()
                .stream()
                .filter(e -> e.getDepartmentId() != null && e.getDepartmentId().equals(departmentId))
                .toList();

        return ResponseEntity.ok(employees);
    }*/

    @GetMapping("/department/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public List<EmployeeDTO> getEmployeesByDepartmentId(@PathVariable Long id) {
        return employeeService.getAllEmployees()
                .stream()
                .filter(emp -> emp.getDepartmentId() != null && emp.getDepartmentId().equals(id))
                .collect(Collectors.toList());
    }

    @GetMapping("/list") // Endpoint pour la liste simplifiée
    public ResponseEntity<List<EmployeeListDTO>> getEmployeeListForDropdown() {
        List<EmployeeListDTO> employees = employeeService.getEmployeeList();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/listemp")
   // @PreAuthorize("isAuthenticated()") // Or specific roles like 'ROLE_HR', 'ROLE_ADMIN'
    public ResponseEntity<List<Map<String, Object>>> getEmployeeList() {
        logger.info("Getting simplified employee list for dropdown");
        List<Employee> employees = employeeRepository.findAll(); // Fetch all employees

        List<Map<String, Object>> employeeList = employees.stream()
                .filter(emp -> emp.getId() != null) // Basic check
                .map(emp -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", emp.getId());

                    // Construct fullName safely
                    String firstName = emp.getName() != null ? emp.getName() : "";
                    String lastName = emp.getLastName() != null ? emp.getLastName() : "";
                    map.put("fullName", (firstName + " " + lastName).trim());

                    // Get department name safely
                    // Import com.bezkoder.springjwt.models.Department; if needed
                    Department department = emp.getDepartment(); // Get the associated Department object
                    String departmentName = (department != null && department.getDepartmentName() != null)
                            ? department.getDepartmentName() // Extract the name
                            : "N/A"; // Default if no department or name is null
                    map.put("departmentName", departmentName); // Add it to the map

                    return map;
                })
                // Sort alphabetically by fullName (optional but recommended)
                // Import java.util.Comparator; if needed
                .sorted(Comparator.comparing(map -> (String) map.get("fullName")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(employeeList);
    }



}
