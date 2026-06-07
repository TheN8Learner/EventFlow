package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.CategoryResponseDto;
import com.eventflow.eventflow.dto.EventRequestDto;
import com.eventflow.eventflow.dto.EventResponseDto;
import com.eventflow.eventflow.model.*;
import com.eventflow.eventflow.repository.CategoryRepository;
import com.eventflow.eventflow.repository.EventRepository;
import com.eventflow.eventflow.repository.RegistrationRepository;
import com.eventflow.eventflow.repository.UserEventRoleRepository;
import com.eventflow.eventflow.repository.UserRepository;
import com.eventflow.eventflow.util.InputSanitizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import com.eventflow.eventflow.exceptions.ResourceNotFoundException;
import com.eventflow.eventflow.exceptions.BadRequestException;
import com.eventflow.eventflow.dtos.UserResponseDto;
import com.eventflow.eventflow.model.Registration;
import com.eventflow.eventflow.model.RegistrationStatus;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserEventRoleRepository userEventRoleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
        private final RegistrationRepository registrationRepository;

        public EventService(EventRepository eventRepository, UserEventRoleRepository userEventRoleRepository, UserRepository userRepository, CategoryRepository categoryRepository, RegistrationRepository registrationRepository) {
                this.eventRepository = eventRepository;
                this.userEventRoleRepository = userEventRoleRepository;
                this.userRepository = userRepository;
                this.categoryRepository = categoryRepository;
                this.registrationRepository = registrationRepository;
        }

    public Page<EventResponseDto> getEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findAll(pageable);

        return events.map(this::toResponseDto);
    }

        public Page<UserResponseDto> getParticipantsForMyEvent(Long eventId, Pageable pageable) {
                Event event = eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event not found"));
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (event.getCreator() == null || event.getCreator().getId() != user.getId()) {
                        throw new BadRequestException("Not authorized to view participants for this event");
                }

                Page<Registration> regs = registrationRepository.findByEventAndStatus(event, RegistrationStatus.CONFIRMED, pageable);
                return regs.map(reg -> {
                        User u = reg.getUser();
                        return new UserResponseDto(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), u.getRole());
                });
        }

    public Page<EventResponseDto> getPublishedEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findByStatus(EventStatus.PUBLISHED, pageable);

        return events.map(this::toResponseDto);
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Event with id " + id + " not found")
        );
    }

    public Page<EventResponseDto> getMyCreatedEvents(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Event> events = eventRepository.findByCreatorId(creator.getId(), pageable);

        return events.map(this::toResponseDto);
    }

    public Page<EventResponseDto> getMyJoinedEvents(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Registration> regs = registrationRepository.findByUserAndStatusNot(user, RegistrationStatus.CANCELLED, pageable);

        List<EventResponseDto> events = regs.getContent()
                .stream()
                .map(Registration::getEvent)
                .filter(event -> event.getCreator() == null || event.getCreator().getId() != user.getId())
                .map(this::toResponseDto)
                .toList();

        return new PageImpl<>(events, pageable, events.size());
    }

        public EventResponseDto updateMyEvent(Long eventId, EventRequestDto eventRequest) {
                Event event = eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event not found"));

                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (event.getCreator() == null || event.getCreator().getId() != user.getId()) {
                        throw new BadRequestException("Not authorized to update this event");
                }

                List<Category> categories = new ArrayList<>();
                for (long id : eventRequest.getCategoryIds()) {
                        Category category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
                        categories.add(category);
                }

                event.setTitle(InputSanitizer.text(eventRequest.getTitle()));
                event.setDescription(InputSanitizer.multiline(eventRequest.getDescription()));
                event.setFlyer(InputSanitizer.text(eventRequest.getFlyer()));
                event.setDate(eventRequest.getDate());
                event.setCapacityMax(eventRequest.getCapacityMax());
                event.setCategories(categories);

                Event saved = eventRepository.save(event);
                syncCapacityStatus(saved);

                return toResponseDto(saved);
        }

        public EventResponseDto cancelMyEvent(Long eventId) {
                Event event = getOwnedEvent(eventId);

                event.setStatus(EventStatus.CANCELLED);
                Event saved = eventRepository.save(event);

                return toResponseDto(saved);
        }

        public EventResponseDto draftMyEvent(Long eventId) {
                Event event = getOwnedEvent(eventId);

                event.setStatus(EventStatus.DRAFT);
                Event saved = eventRepository.save(event);

                return toResponseDto(saved);
        }

        public EventResponseDto publishMyEvent(Long eventId) {
                Event event = getOwnedEvent(eventId);

                long registeredCount = countConfirmedParticipants(event);
                if (isFull(event, registeredCount)) {
                        event.setStatus(EventStatus.COMPLETED);
                } else {
                        event.setStatus(EventStatus.PUBLISHED);
                }
                Event saved = eventRepository.save(event);

                return toResponseDto(saved);
        }


    public EventResponseDto createEvent(EventRequestDto eventRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Category> categories = new ArrayList<>();

        for(long id : eventRequest.getCategoryIds()) {
            Category category = categoryRepository.findById(id).orElseThrow();
            categories.add(category);
        }
        
        Event event = new Event(
                InputSanitizer.text(eventRequest.getTitle()),
                InputSanitizer.multiline(eventRequest.getDescription()),
                InputSanitizer.text(eventRequest.getFlyer()),
                eventRequest.getDate(),
                eventRequest.getCapacityMax(),
                EventStatus.PUBLISHED,
                creator,
                categories
        );

        event.setRegistrations(new ArrayList<>());

        Event savedEvent = eventRepository.save(event);

        UserEventRole userEventRole = new UserEventRole(
                creator,
                savedEvent,
                EventRole.ORGANISATEUR
        );

        userEventRoleRepository.save(userEventRole);

        return toResponseDto(savedEvent);
    }

    public EventResponseDto getEventDetails(Long id) {
        return toResponseDto(getEventById(id));
    }

    private EventResponseDto toResponseDto(Event event) {
        event = syncCapacityStatus(event);
        Long registeredCount = countConfirmedParticipants(event);
        Long capacityMax = event.getCapacityMax();
        Long availableSeats = capacityMax == null ? null : Math.max(0, capacityMax - registeredCount);

        EventResponseDto dto = new EventResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getFlyer(),
                event.getDate(),
                capacityMax,
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
        );
        dto.setRegisteredCount(registeredCount);
        dto.setAvailableSeats(availableSeats);
        return dto;
    }

    private Event getOwnedEvent(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (event.getCreator() == null || event.getCreator().getId() != user.getId()) {
            throw new BadRequestException("Not authorized to manage this event");
        }

        return event;
    }

    private Event syncCapacityStatus(Event event) {
        if (event.getCapacityMax() == null || event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.DRAFT) {
            return event;
        }

        long registeredCount = countConfirmedParticipants(event);
        if (event.getStatus() == EventStatus.PUBLISHED && isFull(event, registeredCount)) {
            event.setStatus(EventStatus.COMPLETED);
            return eventRepository.save(event);
        }

        if (event.getStatus() == EventStatus.COMPLETED && !isFull(event, registeredCount)) {
            event.setStatus(EventStatus.PUBLISHED);
            return eventRepository.save(event);
        }

        return event;
    }

    private boolean isFull(Event event, long registeredCount) {
        return event.getCapacityMax() != null && registeredCount >= event.getCapacityMax();
    }

    private long countConfirmedParticipants(Event event) {
        if (event.getCreator() == null) {
            return registrationRepository.countByEventAndStatus(event, RegistrationStatus.CONFIRMED);
        }

        return registrationRepository.countByEventAndStatusAndUserNot(event, RegistrationStatus.CONFIRMED, event.getCreator());
    }
}
