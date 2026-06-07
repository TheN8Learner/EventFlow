package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.CategoryResponseDto;
import com.eventflow.eventflow.dto.EventRequestDto;
import com.eventflow.eventflow.dto.EventResponseDto;
import com.eventflow.eventflow.model.*;
import com.eventflow.eventflow.repository.CategoryRepository;
import com.eventflow.eventflow.repository.EventRepository;
import com.eventflow.eventflow.repository.UserEventRoleRepository;
import com.eventflow.eventflow.repository.UserRepository;
import com.sun.jdi.request.EventRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.eventflow.eventflow.exceptions.ResourceNotFoundException;
import com.eventflow.eventflow.exceptions.BadRequestException;
import org.springframework.data.domain.PageRequest;
import com.eventflow.eventflow.exceptions.ResourceNotFoundException;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserEventRoleRepository userEventRoleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public EventService(EventRepository eventRepository, UserEventRoleRepository userEventRoleRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.userEventRoleRepository = userEventRoleRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<EventResponseDto> getEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findAll(pageable);

        return events.map(event -> new EventResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getFlyer(),
                event.getDate(),
                event.getCapacityMax(),
                event.getStatus(),
                event.getCreator().getId(),
                event.getCreator().getLastName(),
                event.getCreator().getEmail(),
                event.getCategories()
                        .stream()
                        .map(category -> new CategoryResponseDto(
                                category.getId(),
                                category.getName()
                        ))
                        .toList()
        ));
    }

    public Page<EventResponseDto> getPublishedEvents(Pageable pageable) {
        Page<Event> events = eventRepository.findByStatus(EventStatus.PUBLISHED, pageable);

        return events.map(event -> new EventResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getFlyer(),
                event.getDate(),
                event.getCapacityMax(),
                event.getStatus(),
                event.getCreator().getId(),
                event.getCreator().getLastName(),
                event.getCreator().getEmail(),
                event.getCategories()
                        .stream()
                        .map(category -> new CategoryResponseDto(
                                category.getId(),
                                category.getName()
                        ))
                        .toList()
        ));
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Event with id " + id + " not found")
        );
    }

    public Page<EventResponseDto> getMyCreatedEvents(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User creator = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Event> events = eventRepository.findByCreator(creator, pageable);

        return events.map(event -> new EventResponseDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getFlyer(),
                event.getDate(),
                event.getCapacityMax(),
                event.getStatus(),
                event.getCreator().getId(),
                event.getCreator().getLastName(),
                event.getCreator().getEmail(),
                event.getCategories()
                        .stream()
                        .map(category -> new CategoryResponseDto(
                                category.getId(),
                                category.getName()
                        ))
                        .toList()
        ));
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

                event.setTitle(eventRequest.getTitle());
                event.setDescription(eventRequest.getDescription());
                event.setFlyer(eventRequest.getFlyer());
                event.setDate(eventRequest.getDate());
                event.setCapacityMax(eventRequest.getCapacityMax());
                event.setCategories(categories);

                Event saved = eventRepository.save(event);

                return new EventResponseDto(
                                saved.getId(),
                                saved.getTitle(),
                                saved.getDescription(),
                                saved.getFlyer(),
                                saved.getDate(),
                                saved.getCapacityMax(),
                                saved.getStatus(),
                                saved.getCreator().getId(),
                                saved.getCreator().getLastName(),
                                saved.getCreator().getEmail(),
                                saved.getCategories()
                                                .stream()
                                                .map(category -> new CategoryResponseDto(
                                                                category.getId(),
                                                                category.getName()
                                                ))
                                                .toList()
                );
        }

        public EventResponseDto cancelMyEvent(Long eventId) {
                Event event = eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event not found"));

                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

                if (event.getCreator() == null || event.getCreator().getId() != user.getId()) {
                        throw new BadRequestException("Not authorized to cancel this event");
                }

                event.setStatus(EventStatus.CANCELLED);
                Event saved = eventRepository.save(event);

                return new EventResponseDto(
                                saved.getId(),
                                saved.getTitle(),
                                saved.getDescription(),
                                saved.getFlyer(),
                                saved.getDate(),
                                saved.getCapacityMax(),
                                saved.getStatus(),
                                saved.getCreator().getId(),
                                saved.getCreator().getLastName(),
                                saved.getCreator().getEmail(),
                                saved.getCategories()
                                                .stream()
                                                .map(category -> new CategoryResponseDto(
                                                                category.getId(),
                                                                category.getName()
                                                ))
                                                .toList()
                );
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
                eventRequest.getTitle(),
                eventRequest.getDescription(),
                eventRequest.getFlyer(),
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

        return new EventResponseDto(
                savedEvent.getId(),
                savedEvent.getTitle(),
                savedEvent.getDescription(),
                savedEvent.getFlyer(),
                savedEvent.getDate(),
                savedEvent.getCapacityMax(),
                savedEvent.getStatus(),
                savedEvent.getCreator().getId(),
                savedEvent.getCreator().getLastName(),
                savedEvent.getCreator().getEmail(),
                savedEvent.getCategories()
                        .stream()
                        .map(category -> new CategoryResponseDto(
                                category.getId(),
                                category.getName()
                        ))
                        .toList()        );
    }
}
