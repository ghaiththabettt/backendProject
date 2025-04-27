package com.bezkoder.springjwt.controllers;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.bezkoder.springjwt.models.HRModuleEntities.Contract;
import com.bezkoder.springjwt.models.HRModuleEntities.Department;
import com.bezkoder.springjwt.payload.request.*;
import com.bezkoder.springjwt.repository.HRModuleRepository.ContractRepository;
import com.bezkoder.springjwt.repository.HRModuleRepository.DepartmentRepository;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.bezkoder.springjwt.models.*;
import com.bezkoder.springjwt.payload.response.JwtResponse;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import com.bezkoder.springjwt.repository.*;
import com.bezkoder.springjwt.security.jwt.JwtUtils;
import com.bezkoder.springjwt.security.services.PasswordResetService;
import com.bezkoder.springjwt.security.services.UserDetailsImpl;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    ContractRepository contractRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;
    
    @Autowired
    PasswordResetService passwordResetService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getName(),
                userDetails.getLastName(),
                userDetails.getEmail(),
                roles,
                userDetails.getUserType()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user based on type
        User user;
        
        switch (signUpRequest.getUserType()) {
            case ROLE_CUSTOMER:
                Customer customer = new Customer(
                    signUpRequest.getName(),
                    signUpRequest.getLastName(),
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()),
                    signUpRequest.getCompanyName()
                );
                customer.setAddress(signUpRequest.getAddress());
                customer.setPhoneNumber(signUpRequest.getPhoneNumber());
                customer.setUserType(EUserType.ROLE_CUSTOMER);
                user = customerRepository.save(customer);
                break;

            case ROLE_EMPLOYEE:
                EmployeeSignupRequest empReq = (EmployeeSignupRequest) signUpRequest;

                Department department = null;
                if (empReq.getDepartmentId() != null) {
                    department = departmentRepository.findById(empReq.getDepartmentId())
                            .orElseThrow(() -> new RuntimeException("Département introuvable"));
                }

                Contract contract = null;
                if (empReq.getContractId() != null) {
                    contract = contractRepository.findById(empReq.getContractId())
                            .orElseThrow(() -> new RuntimeException("Contrat introuvable"));
                }

                Employee employee = new Employee(
                        empReq.getName(),
                        empReq.getLastName(),
                        empReq.getEmail(),
                        encoder.encode(empReq.getPassword()),
                        empReq.getSalary(),
                        empReq.getHireDate(), // Doit renvoyer un LocalDate valide
                        empReq.getPosition()  // Conversion éventuelle en enum si besoin
                );
                employee.setAddress(empReq.getAddress());
                employee.setPhoneNumber(empReq.getPhoneNumber());
                employee.setDepartment(department);
                employee.setContract(contract);
                employee.setUserType(EUserType.ROLE_EMPLOYEE);
                user = employeeRepository.save(employee);
                break;



            case ROLE_ADMIN:
                User admin = new User(
                        signUpRequest.getName(),
                        signUpRequest.getLastName(),
                        signUpRequest.getEmail(),
                        encoder.encode(signUpRequest.getPassword())
                );
                admin.setUserType(EUserType.ROLE_ADMIN);
                user = userRepository.save(admin);
                break;

            case ROLE_HR:  // ✅ Ajout du rôle RH
                System.out.println("Registering HR user...");
                Employee hrEmployee = new Employee(
                        signUpRequest.getName(),
                        signUpRequest.getLastName(),
                        signUpRequest.getEmail(),
                        encoder.encode(signUpRequest.getPassword()),
                        signUpRequest.getSalary(),
                        (LocalDate) signUpRequest.getHireDate(),
                        signUpRequest.getPosition()
                );
                hrEmployee.setAddress(signUpRequest.getAddress());
                hrEmployee.setPhoneNumber(signUpRequest.getPhoneNumber());
                hrEmployee.setUserType(EUserType.ROLE_HR);
                user = employeeRepository.save(hrEmployee);
                System.out.println("HR Employee saved with ID: " + user.getId());
                break;

            default:
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Invalid user type!"));
        }

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String baseUrl = servletRequest.getScheme() + "://" + servletRequest.getServerName();
            
            // Add port if it's not the default port
            if (servletRequest.getServerPort() != 80 && servletRequest.getServerPort() != 443) {
                baseUrl += ":" + servletRequest.getServerPort();
            }
            
            // Add context path if present
            if (servletRequest.getContextPath() != null && !servletRequest.getContextPath().isEmpty()) {
                baseUrl += servletRequest.getContextPath();
            }
            
            boolean result = passwordResetService.createPasswordResetTokenForUser(request.getEmail(), baseUrl);
            
            // Always return the same message for security reasons
            return ResponseEntity.ok(new MessageResponse("If your email exists in our system, you will receive a password reset link shortly"));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: An unexpected error occurred. Please try again later."));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            if (!passwordResetService.validatePasswordResetToken(request.getToken())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Invalid or expired token. Please request a new password reset link."));
            }
            
            boolean result = passwordResetService.resetPassword(request.getToken(), request.getPassword());
            
            if (result) {
                return ResponseEntity.ok(new MessageResponse("Your password has been reset successfully. You can now log in with your new password."));
            } else {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Failed to reset password. Please try again or request a new reset link."));
            }
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: An unexpected error occurred. Please try again later."));
        }
    }




    @PostMapping("/admin/signup")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROLE_HR')")
    public ResponseEntity<?> registerEmployee(@Valid @RequestBody EmployeeSignupRequest signUpRequest) {
        // Check if email is already in use
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Retrieve the Department entity based on the provided departmentId
        Department department = null;
        // departmentId is mandatory in your frontend, but backend check is still good practice
        if (signUpRequest.getDepartmentId() != null) {
            department = departmentRepository.findById(signUpRequest.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Département introuvable avec ID: " + signUpRequest.getDepartmentId()));
            // Added ID to error message for clarity
        }
        // Note: If departmentId is mandatory on frontend AND backend DTO validation (@NotNull),
        // you might not need the null check here, but the findById().orElseThrow() is still needed.


        // *** REMOVE the logic related to the single contract field ***
        // The single 'Contract contract' field has been removed from the Employee entity.
        // A contract should be assigned separately via the /api/Contract endpoint.

        // Remove these lines if they were present and using the removed 'contract' field:
        // Contract contract = null;
        // if (signUpRequest.getContractId() != null) {
        //     contract = contractRepository.findById(signUpRequest.getContractId())
        //             .orElseThrow(() -> new RuntimeException("Contrat introuvable"));
        // }
        // employee.setContract(contract); // <--- THIS LINE MUST BE REMOVED


        // Convert the position string from DTO to enum
        EEmployeePosition positionEnum;
        try {
            positionEnum = EEmployeePosition.valueOf(String.valueOf(signUpRequest.getPosition())); // Assume position is already string in DTO
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid position value: " + signUpRequest.getPosition()));
            // Added value to error message
        }

        // Create the Employee entity
        Employee employee = new Employee(
                signUpRequest.getName(),
                signUpRequest.getLastName(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()), // Encode password
                signUpRequest.getSalary(),
                signUpRequest.getHireDate(), // Assumes HireDate is LocalDate in DTO
                positionEnum
        );

        // Set optional fields and relationships
        employee.setAddress(signUpRequest.getAddress());
        employee.setPhoneNumber(signUpRequest.getPhoneNumber());
        employee.setDepartment(department);
        // The contracts list will be empty by default for a new Employee entity.
        // Contracts will be added later via the contract creation endpoint.
        employee.setDateOfBirth(signUpRequest.getDateOfBirth()); // Assumes DateOfBirth is Date in DTO
        employee.setUserType(EUserType.ROLE_EMPLOYEE); // Explicitly set user type for this endpoint

        // Save the Employee entity
        employeeRepository.save(employee);

        // Return a success message
        return ResponseEntity.ok(new MessageResponse("Employee registered successfully!"));
    }



}
