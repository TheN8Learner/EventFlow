package com.eventflow.eventflow.controller;

import com.eventflow.eventflow.dto.ChangeUserRoleRequestDto;
import com.eventflow.eventflow.dto.EventResponseDto;
import com.eventflow.eventflow.dto.RegistrationResponseDto;
import com.eventflow.eventflow.dtos.UserResponseDto;
import com.eventflow.eventflow.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin")
@Validated
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponseDto> changeUserRole(@PathVariable Long id, @Valid @RequestBody ChangeUserRoleRequestDto request) {
        return ResponseEntity.ok(adminService.changeUserRole(id, request));
    }

    @GetMapping("/registrations")
    public ResponseEntity<Page<RegistrationResponseDto>> getAllRegistrations(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllRegistrations(pageable));
    }

    @GetMapping("/events")
    public ResponseEntity<Page<EventResponseDto>> getAllEvents(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllEventsAdmin(pageable));
    }
}
