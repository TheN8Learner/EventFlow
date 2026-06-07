package com.eventflow.eventflow.controller;

import com.eventflow.eventflow.dto.ChangePasswordRequestDto;
import com.eventflow.eventflow.dto.LoginRequestDto;
import com.eventflow.eventflow.dto.LoginResponseDto;
import com.eventflow.eventflow.dto.RefreshTokenRequestDto;
import com.eventflow.eventflow.dto.RegisterRequestDto;
import com.eventflow.eventflow.dto.UpdateUserRequestDto;
import com.eventflow.eventflow.dtos.UserResponseDto;
import com.eventflow.eventflow.service.AuthService;
import com.eventflow.eventflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserContoller {
    private final AuthService authService;
    private final UserService userService;

    public UserContoller(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDto> registerUser(@Valid @RequestBody RegisterRequestDto requestDto) {
        try {
            LoginResponseDto tokens = authService.register(requestDto);
            return ResponseEntity.ok(tokens);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUser(@Valid @RequestBody LoginRequestDto requestDto) {
        try {
            LoginResponseDto tokens = authService.login(requestDto);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto requestDto) {
        return ResponseEntity.ok(authService.refreshToken(requestDto.getRefreshToken()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMyProfile(@Valid @RequestBody UpdateUserRequestDto requestDto) {
        return ResponseEntity.ok(userService.updateMyProfile(requestDto));
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequestDto requestDto) {
        userService.changePassword(requestDto);
        return ResponseEntity.ok().build();
    }
}
