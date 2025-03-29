package com.bezkoder.springjwt.security.services;

import com.bezkoder.springjwt.models.PasswordResetToken;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.repository.PasswordResetTokenRepository;
import com.bezkoder.springjwt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Creates a password reset token for the user with the given email and sends a reset email
     * @param email The email of the user requesting a password reset
     * @return true if the reset token was created and email sent, false if the user doesn't exist
     */
    public boolean createPasswordResetTokenForUser(String email, String baseUrl) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                return false;
            }

            User user = userOptional.get();
            
            // Delete any existing token for this user
            tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
            
            // Create new token
            String token = UUID.randomUUID().toString();
            PasswordResetToken myToken = new PasswordResetToken(
                    token, 
                    user,
                    Instant.now().plus(24, ChronoUnit.HOURS)
            );
            tokenRepository.save(myToken);
            
            // Send email with reset link
            String resetUrl = baseUrl + "/reset-password?token=" + token;
            
            // Create a more user-friendly email
            String emailBody = 
                    "Dear " + user.getName() + " " + user.getLastName() + ",\n\n" +
                    "We received a request to reset your password for your DevApex account. " +
                    "If you didn't make this request, you can safely ignore this email.\n\n" +
                    "To reset your password, please click on the link below or copy and paste it into your browser:\n\n" +
                    resetUrl + "\n\n" +
                    "This link will expire in 24 hours for security reasons.\n\n" +
                    "If you have any questions or need assistance, please contact our support team.\n\n" +
                    "Best regards,\n" +
                    "The DevApex Team";
            
            // Send the email and log any issues
            try {
                emailService.sendSimpleMessage(user.getEmail(), "DevApex - Password Reset Request", emailBody);
            } catch (Exception e) {
                System.err.println("Failed to send password reset email: " + e.getMessage());
                e.printStackTrace();
                // Continue with the process even if email sending fails
                // The token is still created and can be used
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error in createPasswordResetTokenForUser: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Validates a password reset token
     * @param token The token to validate
     * @return true if the token is valid and not expired, false otherwise
     */
    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        if (tokenOptional.isEmpty()) {
            return false;
        }
        
        PasswordResetToken resetToken = tokenOptional.get();
        return !resetToken.isExpired();
    }

    /**
     * Resets the password for the user associated with the given token
     * @param token The password reset token
     * @param newPassword The new password
     * @return true if the password was reset successfully, false otherwise
     */
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);
        if (tokenOptional.isEmpty() || tokenOptional.get().isExpired()) {
            return false;
        }
        
        User user = tokenOptional.get().getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Delete the used token
        tokenRepository.delete(tokenOptional.get());
        
        return true;
    }
}
