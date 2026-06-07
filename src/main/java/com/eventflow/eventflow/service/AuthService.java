package com.eventflow.eventflow.service;

import com.eventflow.eventflow.component.JwtUtils;
import com.eventflow.eventflow.dto.LoginResponseDto;
import com.eventflow.eventflow.dto.LoginRequestDto;
import com.eventflow.eventflow.dto.RegisterRequestDto;
import com.eventflow.eventflow.exceptions.BadRequestException;
import com.eventflow.eventflow.exceptions.ForbiddenException;
import com.eventflow.eventflow.exceptions.ResourceNotFoundException;
import com.eventflow.eventflow.model.Role;
import com.eventflow.eventflow.model.User;
import com.eventflow.eventflow.repository.UserRepository;
import com.eventflow.eventflow.util.InputSanitizer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public LoginResponseDto register(RegisterRequestDto requestDto) {
        String email = InputSanitizer.email(requestDto.getEmail());
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("Email already in use");
        }

        User user = new User(
                InputSanitizer.text(requestDto.getFirstName()),
                InputSanitizer.text(requestDto.getLastName()),
                email,
                passwordEncoder.encode(requestDto.getPassword())
        );
        user.setRole(Role.USER);
        user.setEvents(new ArrayList<>());
        user.setEventRoles(new ArrayList<>());
        user.setRegistrations(new ArrayList<>());
        userRepository.save(user);

        return generateTokens(user.getEmail());
    }

    public LoginResponseDto login(LoginRequestDto requestDto) {
        String email = InputSanitizer.email(requestDto.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        requestDto.getPassword()
                )
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return generateTokens(user.getEmail());
    }

    public LoginResponseDto registerAdmin(RegisterRequestDto requestDto) {
        String email = InputSanitizer.email(requestDto.getEmail());
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("Email already in use");
        }

        User user = new User(
                InputSanitizer.text(requestDto.getFirstName()),
                InputSanitizer.text(requestDto.getLastName()),
                email,
                passwordEncoder.encode(requestDto.getPassword())
        );
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        return generateTokens(user.getEmail());
    }

    public LoginResponseDto adminLogin(LoginRequestDto requestDto) {
        String email = InputSanitizer.email(requestDto.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        requestDto.getPassword()
                )
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Access denied: not an admin");
        }

        return generateTokens(user.getEmail());
    }

    public LoginResponseDto refreshToken(String refreshToken) {
        if (!jwtUtils.validateRefreshToken(refreshToken)) {
            throw new ForbiddenException("Invalid refresh token");
        }

        String email = jwtUtils.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return generateTokens(user.getEmail());
    }

    private LoginResponseDto generateTokens(String email) {
        return new LoginResponseDto(
                jwtUtils.generateAccessToken(email),
                jwtUtils.generateRefreshToken(email)
        );
    }
}
