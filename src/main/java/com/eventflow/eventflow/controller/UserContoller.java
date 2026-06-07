package com.eventflow.eventflow.controller;

import com.eventflow.eventflow.dto.LoginRequestDto;
import com.eventflow.eventflow.dto.RegisterRequestDto;
import com.eventflow.eventflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserContoller {
    private final UserService userService;

    public UserContoller(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDto requestDto) {
        try {
            String token = userService.registerUser(requestDto);
            return ResponseEntity.ok(token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequestDto requestDto) {
        try {
            String token = userService.loginUser(requestDto);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/auth/admin/register")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody RegisterRequestDto requestDto) {
        try {
            String token = userService.registerAdmin(requestDto);
            return ResponseEntity.ok(token);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/auth/admin/login")
    public ResponseEntity<?> loginAdmin(@Valid @RequestBody LoginRequestDto requestDto) {
        try {
            String message = userService.loginAdmin(requestDto);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
