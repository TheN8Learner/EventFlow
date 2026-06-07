package com.eventflow.eventflow.service;

import com.eventflow.eventflow.component.JwtUtils;
import com.eventflow.eventflow.dto.LoginRequestDto;
import com.eventflow.eventflow.dto.RegisterRequestDto;
import com.eventflow.eventflow.model.Role;
import com.eventflow.eventflow.model.User;
import com.eventflow.eventflow.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.eventflow.eventflow.exceptions.ResourceNotFoundException;
import com.eventflow.eventflow.exceptions.ForbiddenException;

import java.util.ArrayList;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public String registerUser(RegisterRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
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

    public String loginUser(LoginRequestDto requestDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.getEmail(),
                        requestDto.getPassword()
                )
        );

        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            return jwtUtils.generateToken(requestDto.getEmail());
        }

        throw new ResourceNotFoundException("User not found");
    }

    public String registerAdmin(RegisterRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
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

    @PreAuthorize("hasRole(ADMIN)")
    public String loginAdmin(LoginRequestDto requestDto) {
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

        return "Admin Login successful";
    }
}
