package com.bezkoder.springjwt.controllers;

import com.bezkoder.springjwt.models.Employee;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import com.bezkoder.springjwt.repository.EmployeeRepository;
import com.bezkoder.springjwt.repository.UserRepository;
import com.bezkoder.springjwt.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Create response with user data
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("firstName", user.getName());
            response.put("lastName", user.getLastName());
            response.put("email", user.getEmail());
            response.put("roles", user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList()));
            
            // Check if user is an employee and add position
            if (user.getUserType() != null && user.getUserType().name().equals("ROLE_EMPLOYEE")) {
                Optional<Employee> employeeOptional = employeeRepository.findById(user.getId());
                if (employeeOptional.isPresent()) {
                    Employee employee = employeeOptional.get();
                    if (employee.getPosition() != null) {
                        response.put("position", employee.getPosition().name());
                    } else {
                        response.put("position", "");
                    }
                }
            }
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: User not found!"));
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<?> updateUserProfile(@RequestBody Map<String, Object> userData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Update user fields if provided
            if (userData.containsKey("firstName")) {
                user.setName((String) userData.get("firstName"));
            }
            
            if (userData.containsKey("lastName")) {
                user.setLastName((String) userData.get("lastName"));
            }
            
            // Save updated user
            userRepository.save(user);
            
            // If user is an employee and position is provided, update position
            if (user.getUserType() != null && user.getUserType().name().equals("ROLE_EMPLOYEE") && userData.containsKey("position")) {
                Optional<Employee> employeeOptional = employeeRepository.findById(user.getId());
                if (employeeOptional.isPresent()) {
                    // Update employee position logic would go here
                    // This would require converting the string position to EEmployeePosition enum
                }
            }
            
            // Return updated user data
            return getUserProfile();
        } else {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: User not found!"));
        }
    }
}
