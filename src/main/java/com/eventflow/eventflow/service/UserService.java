package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.ChangePasswordRequestDto;
import com.eventflow.eventflow.dto.UpdateUserRequestDto;
import com.eventflow.eventflow.dtos.UserResponseDto;
import com.eventflow.eventflow.exceptions.BadRequestException;
import com.eventflow.eventflow.exceptions.ResourceNotFoundException;
import com.eventflow.eventflow.model.User;
import com.eventflow.eventflow.repository.UserRepository;
import com.eventflow.eventflow.util.InputSanitizer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDto getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new UserResponseDto(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole());
    }

    public UserResponseDto updateMyProfile(UpdateUserRequestDto requestDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newEmail = InputSanitizer.email(requestDto.getEmail());
        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new BadRequestException("Email already in use");
            }
            user.setEmail(newEmail);
        }

        user.setFirstName(InputSanitizer.text(requestDto.getFirstName()));
        user.setLastName(InputSanitizer.text(requestDto.getLastName()));

        userRepository.save(user);

        return new UserResponseDto(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole());
    }

    public void changePassword(ChangePasswordRequestDto requestDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));
        userRepository.save(user);
    }
}
