package com.task_managment_api.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        String verificationLink = "http://localhost:8085/api/v1/auth/verify?token=" + token;

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("taskmanagement.notify@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Account Verification - Task Management System");

            String htmlContent = "<h3>Hello " + fullName + ",</h3>"
                    + "<p>Thank you for registering at Task Management System. To activate your account, please click the link below:</p>"
                    + "<p><a href=\"" + verificationLink + "\" style=\"display: inline-block; padding: 10px 20px; color: white; background-color: #3b82f6; border-radius: 5px; text-decoration: none; font-weight: bold;\">Verify My Email</a></p>"
                    + "<p>If you did not create this account, you can safely ignore this email.</p>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("An error occurred while sending the email.", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        String resetLink = "http://localhost:5173/reset-password?token=" + token;

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("taskmanagement.notify@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Password Reset - Task Management System");

            String htmlContent = "<h3>Hello " + fullName + ",</h3>"
                    + "<p>We received a request to reset your password. Click the button below to set a new password:</p>"
                    + "<p><a href=\"" + resetLink + "\" style=\"display: inline-block; padding: 10px 20px; color: white; background-color: #ef4444; border-radius: 5px; text-decoration: none; font-weight: bold;\">Reset My Password</a></p>"
                    + "<p>This link will expire in <strong>1 hour</strong>.</p>"
                    + "<p>If you did not request a password reset, you can safely ignore this email.</p>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("An error occurred while sending the password reset email.", e);
        }
    }
}

