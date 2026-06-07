package com.eventflow.eventflow.service;

import com.eventflow.eventflow.component.JwtUtils;
import com.eventflow.eventflow.dto.LoginRequestDto;
import com.eventflow.eventflow.dto.RegisterRequestDto;
import com.eventflow.eventflow.exceptions.BadRequestException;
import com.eventflow.eventflow.exceptions.ForbiddenException;
import com.eventflow.eventflow.exceptions.ResourceNotFoundException;
import com.eventflow.eventflow.model.Role;
import com.eventflow.eventflow.model.User;
import com.eventflow.eventflow.repository.UserRepository;
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

    public String register(RegisterRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new BadRequestException("Email already in use");
        }

        User user = new User(
                requestDto.getFirstName(),
                requestDto.getLastName(),
                requestDto.getEmail(),
                passwordEncoder.encode(requestDto.getPassword())
        );
        user.setRole(Role.USER);
        user.setEvents(new ArrayList<>());
        user.setEventRoles(new ArrayList<>());
        user.setRegistrations(new ArrayList<>());
        userRepository.save(user);

        return jwtUtils.generateToken(user.getEmail());
    }

    public String login(LoginRequestDto requestDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.getEmail(),
                        requestDto.getPassword()
                )
        );

        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return jwtUtils.generateToken(user.getEmail());
    }

    public String registerAdmin(RegisterRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new BadRequestException("Email already in use");
        }

        User user = new User(
                requestDto.getFirstName(),
                requestDto.getLastName(),
                requestDto.getEmail(),
                passwordEncoder.encode(requestDto.getPassword())
        );
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        return jwtUtils.generateToken(user.getEmail());
    }

    public String adminLogin(LoginRequestDto requestDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.getEmail(),
                        requestDto.getPassword()
                )
        );

        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Access denied: not an admin");
        }

        return jwtUtils.generateToken(user.getEmail());
    }
}
