package com.bezkoder.springjwt.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bezkoder.springjwt.payload.response.MessageResponse;
import com.bezkoder.springjwt.security.services.EmailService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {
  
  @Autowired
  private EmailService emailService;
  
  @GetMapping("/all")
  public String allAccess() {
    return "Public Content.";
  }
  
  @GetMapping("/user")
  @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
  public String userAccess() {
    return "User Content.";
  }

  @GetMapping("/mod")
  @PreAuthorize("hasRole('MODERATOR')")
  public String moderatorAccess() {
    return "Moderator Board.";
  }

  @GetMapping("/admin")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminAccess() {
    return "Admin Board.";
  }
  
  @GetMapping("/test-email")
  public MessageResponse testEmail(@RequestParam String to) {
    try {
      emailService.sendSimpleMessage(
          to, 
          "Test Email from DevApex", 
          "This is a test email from the DevApex application. If you received this, email sending is working correctly!"
      );
      return new MessageResponse("Test email sent successfully to " + to);
    } catch (Exception e) {
      e.printStackTrace();
      return new MessageResponse("Failed to send test email: " + e.getMessage());
    }
  }
}
