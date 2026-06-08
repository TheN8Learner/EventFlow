package com.eventflow.eventflow.controller;

import com.eventflow.eventflow.dto.RegistrationRequestDto;
import com.eventflow.eventflow.dto.RegistrationResponseDto;
import com.eventflow.eventflow.model.RegistrationStatus;
import com.eventflow.eventflow.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public ResponseEntity<Page<RegistrationResponseDto>> getRegistrations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(registrationService.getMyRegistrations(pageable));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<RegistrationResponseDto>> getMyRegistrations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(registrationService.getMyRegistrations(pageable));
    }

    @GetMapping("/my-created-events")
    public ResponseEntity<Page<RegistrationResponseDto>> getRegistrationsForMyCreatedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(registrationService.getRegistrationsForMyCreatedEvents(pageable));
    }

    @GetMapping("/my-created-events/{eventId}")
    public ResponseEntity<Page<RegistrationResponseDto>> getRegistrationsForMyCreatedEvent(
            @PathVariable Long eventId,
            @RequestParam(required = false) RegistrationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(registrationService.getRegistrationsForMyCreatedEvent(eventId, status, pageable));
    }

    @PostMapping
    public ResponseEntity<RegistrationResponseDto> createRegistration(@Valid @RequestBody RegistrationRequestDto requestDto) {
        return ResponseEntity.ok(registrationService.createRegistration(requestDto));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<RegistrationResponseDto> cancelMyRegistration(@PathVariable Long id) {
        return ResponseEntity.ok(registrationService.cancelMyRegistration(id));
    }
}
