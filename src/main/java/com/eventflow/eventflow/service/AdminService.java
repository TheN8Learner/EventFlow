package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.ChangeUserRoleRequestDto;
import com.eventflow.eventflow.dto.CategoryResponseDto;
import com.eventflow.eventflow.dto.EventResponseDto;
import com.eventflow.eventflow.dto.RegistrationResponseDto;
import com.eventflow.eventflow.dtos.UserResponseDto;
import com.eventflow.eventflow.exceptions.BadRequestException;
import com.eventflow.eventflow.exceptions.ResourceNotFoundException;
import com.eventflow.eventflow.model.Event;
import com.eventflow.eventflow.model.Registration;
import com.eventflow.eventflow.model.Role;
import com.eventflow.eventflow.model.User;
import com.eventflow.eventflow.repository.EventRepository;
import com.eventflow.eventflow.repository.RegistrationRepository;
import com.eventflow.eventflow.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class AdminService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    public AdminService(UserRepository userRepository, EventRepository eventRepository, RegistrationRepository registrationRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    public Page<UserResponseDto> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(u -> new UserResponseDto(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), u.getRole()));
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
    }

    public UserResponseDto changeUserRole(Long userId, ChangeUserRoleRequestDto request) {
        if (request == null || request.getRole() == null) {
            throw new BadRequestException("Role is required");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Role newRole = request.getRole();
        user.setRole(newRole);
        User saved = userRepository.save(user);
        return new UserResponseDto(saved.getId(), saved.getEmail(), saved.getFirstName(), saved.getLastName(), saved.getRole());
    }

    public Page<RegistrationResponseDto> getAllRegistrations(Pageable pageable) {
        Page<Registration> regs = registrationRepository.findAll(pageable);
        return regs.map(reg -> new RegistrationResponseDto(
                reg.getId(),
                reg.getStatus(),
                reg.getEvent() != null ? reg.getEvent().getId() : null,
                reg.getUser() != null ? reg.getUser().getId() : null
        ));
    }

    public Page<EventResponseDto> getAllEventsAdmin(Pageable pageable) {
        Page<Event> events = eventRepository.findAll(pageable);
        return events.map(event -> new EventResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getFlyer(),
                event.getDate(),
                event.getCapacityMax(),
                event.getStatus(),
                event.getCreator() != null ? event.getCreator().getId() : null,
                event.getCreator() != null ? event.getCreator().getLastName() : null,
                event.getCreator() != null ? event.getCreator().getEmail() : null,
                event.getCategories()
                        .stream()
                        .map(category -> new CategoryResponseDto(
                                category.getId(),
                                category.getName()
                        ))
                        .toList()
        ));
    }
}
