package com.bezkoder.springjwt.security.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EmailService {
    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    /**
     * Sends an email using Jakarta Mail API directly
     * @param to recipient email address
     * @param subject email subject
     * @param text email body
     */
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            logger.info("Preparing to send email to: " + to);
            logger.info("From email address: " + username);
            
            // Set mail properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            props.put("mail.debug", "true");
            
            // Create session with authenticator
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(text);
            
            logger.info("Attempting to send email...");
            
            // Send message
            Transport.send(message);
            
            logger.info("Email sent successfully to: " + to);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to send email: " + e.getMessage(), e);
            logger.severe("Error type: " + e.getClass().getName());
            e.printStackTrace();
        }
    }
}
