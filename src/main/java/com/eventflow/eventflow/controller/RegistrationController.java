package com.eventflow.eventflow.controller;

import com.eventflow.eventflow.dto.RegistrationRequestDto;
import com.eventflow.eventflow.dto.RegistrationResponseDto;
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
        return ResponseEntity.ok(registrationService.getRegistrations(pageable));
    }

    @PostMapping
    public ResponseEntity<RegistrationResponseDto> createRegistration(@Valid @RequestBody RegistrationRequestDto requestDto) {
        return ResponseEntity.ok(registrationService.createRegistration(requestDto));
    }
}
