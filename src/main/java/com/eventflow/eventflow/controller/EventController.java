package com.eventflow.eventflow.controller;


import com.eventflow.eventflow.dto.EventRequestDto;
import com.eventflow.eventflow.dto.EventResponseDto;
import com.eventflow.eventflow.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<Page<EventResponseDto>> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(eventService.getEvents(pageable));
    }

    @GetMapping("/my-created")
    public ResponseEntity<Page<EventResponseDto>> getMyCreatedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(eventService.getMyCreatedEvents(pageable));
    }

    @GetMapping("/my-joined")
    public ResponseEntity<Page<EventResponseDto>> getMyJoinedEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(eventService.getMyJoinedEvents(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDto> getEventDetails(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventDetails(id));
    }

    @PostMapping
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody EventRequestDto eventRequestDto) {
        EventResponseDto eventResponseDto = eventService.createEvent(eventRequestDto);
        return ResponseEntity.ok(eventResponseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDto> updateMyEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequestDto eventRequestDto
    ) {
        return ResponseEntity.ok(eventService.updateMyEvent(id, eventRequestDto));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<EventResponseDto> cancelMyEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.cancelMyEvent(id));
    }

    @PostMapping("/{id}/draft")
    public ResponseEntity<EventResponseDto> draftMyEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.draftMyEvent(id));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<EventResponseDto> publishMyEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.publishMyEvent(id));
    }
}
