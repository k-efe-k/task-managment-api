package com.task_managment_api.demo.controller;

import com.task_managment_api.demo.domain.entity.User;
import com.task_managment_api.demo.dto.response.UserResponse;
import com.task_managment_api.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile() {
        return ResponseEntity.ok(UserResponse.fromEntity(getCurrentUser()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody Map<String, String> body) {
        User currentUser = getCurrentUser();

        String fullName = body.get("fullName");
        String email = body.get("email");
        String password = body.get("password");

        if (fullName != null && !fullName.isBlank()) {
            currentUser.setFullName(fullName);
        }
        if (email != null && !email.isBlank()) {
            if (!email.equalsIgnoreCase(currentUser.getEmail()) && userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email address is already in use.");
            }
            currentUser.setEmail(email);
        }
        if (password != null && !password.isBlank()) {
            currentUser.setPassword(passwordEncoder.encode(password));
        }

        User savedUser = userRepository.save(currentUser);
        return ResponseEntity.ok(UserResponse.fromEntity(savedUser));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }
        List<User> users = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
        return ResponseEntity.ok(users.stream()
                .map(UserResponse::fromEntity)
                .toList());
    }
}
