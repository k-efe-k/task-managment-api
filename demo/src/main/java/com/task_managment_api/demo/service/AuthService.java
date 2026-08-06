package com.task_managment_api.demo.service;

import com.task_managment_api.demo.domain.entity.User;
import com.task_managment_api.demo.dto.request.LoginRequest;
import com.task_managment_api.demo.dto.request.RegisterRequest;
import com.task_managment_api.demo.dto.response.AuthResponse;
import com.task_managment_api.demo.repository.UserRepository;
import com.task_managment_api.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    private User getCurrentUser() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("This email address is already in use.");
        }

        String verificationToken = UUID.randomUUID().toString();

        var user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.MEMBER)
                .enabled(true)
                .verificationToken(verificationToken)
                .build();

        userRepository.save(user);

        try {
            emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verificationToken);
        } catch (Exception e) {
            System.err.println("=== EMAIL SENDING FAILED ===");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Activation link for local development:");
            System.err.println("http://localhost:8085/api/v1/auth/verify?token=" + verificationToken);
            System.err.println("========================================");
        }

        return AuthResponse.builder()
                .token(null)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        // Auto-enable user if password is correct (convenient for local dev and testing)
        if (Boolean.FALSE.equals(user.getEnabled()) || user.getEnabled() == null) {
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                user.setEnabled(true);
                user.setVerificationToken(null);
                userRepository.save(user);
            }
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (org.springframework.security.authentication.DisabledException e) {
            throw new RuntimeException("Please verify your email address. Click the verification link sent to your email.");
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new RuntimeException("Invalid email or password.");
        }

        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    public void verifyUser(String token) {
        var user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token."));
        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    // ── Şifremi Unuttum ──────────────────────────────────────────────────────
    public void forgotPassword(String email) {
        // Güvenlik: email bulunamasa da hata vermiyoruz (enumeration attack önlemi)
        userRepository.findByEmail(email).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            try {
                emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetToken);
            } catch (Exception e) {
                System.err.println("=== PASSWORD RESET EMAIL FAILED ===");
                System.err.println("Reset link: http://localhost:5173/reset-password?token=" + resetToken);
                System.err.println("====================================");
            }
        });
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired password reset link."));

        if (user.getPasswordResetExpiry() == null || LocalDateTime.now().isAfter(user.getPasswordResetExpiry())) {
            throw new RuntimeException("Password reset link has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);
    }
}
