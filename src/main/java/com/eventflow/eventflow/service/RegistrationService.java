package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.RegistrationRequestDto;
import com.eventflow.eventflow.dto.RegistrationResponseDto;
import com.eventflow.eventflow.model.*;
import com.eventflow.eventflow.repository.EventRepository;
import com.eventflow.eventflow.repository.RegistrationRepository;
import com.eventflow.eventflow.repository.UserRepository;
import com.eventflow.eventflow.exceptions.ResourceNotFoundException;
import com.eventflow.eventflow.exceptions.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistrationService {

	private final RegistrationRepository registrationRepository;
	private final EventRepository eventRepository;
	private final UserRepository userRepository;

	public RegistrationService(RegistrationRepository registrationRepository, EventRepository eventRepository, UserRepository userRepository) {
		this.registrationRepository = registrationRepository;
		this.eventRepository = eventRepository;
		this.userRepository = userRepository;
	}


	// new API: registerToEvent (alias to existing createRegistration logic)
	public RegistrationResponseDto registerToEvent(RegistrationRequestDto requestDto) {
		return createRegistration(requestDto);
	}

	public Page<RegistrationResponseDto> getMyRegistrations(Pageable pageable) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		Page<Registration> registrations = registrationRepository.findByUser(user, pageable);
		return registrations.map(this::toResponseDto);
	}

	public Page<RegistrationResponseDto> getRegistrationsForMyCreatedEvents(Pageable pageable) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		List<Event> events = eventRepository.findByCreator(user, Pageable.unpaged()).getContent();

		if (events.isEmpty()) {
			return new PageImpl<>(List.of(), pageable, 0);
		}

		return registrationRepository.findByEventInAndUserNot(events, user, pageable)
				.map(this::toResponseDto);
	}

	public RegistrationResponseDto cancelMyRegistration(Long registrationId) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Registration reg = registrationRepository.findById(registrationId)
				.orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

		if (reg.getUser() == null || reg.getUser().getId() != user.getId()) {
			throw new BadRequestException("Cannot cancel another user's registration");
		}

		reg.setStatus(RegistrationStatus.CANCELLED);
		registrationRepository.save(reg);
		reopenEventIfSeatFreed(reg.getEvent());

		return new RegistrationResponseDto(reg.getId(), reg.getStatus(), reg.getEvent().getId(), reg.getUser().getId());
	}

	public Page<RegistrationResponseDto> getParticipantsForMyEvent(Long eventId, Pageable pageable) {
		Event event = eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event not found"));
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (event.getCreator() == null || event.getCreator().getId() != user.getId()) {
			throw new BadRequestException("Not authorized to view participants for this event");
		}

		Page<Registration> regs = registrationRepository.findByEventAndUserNot(event, user, pageable);
		return regs.map(this::toResponseDto);
	}

	public RegistrationResponseDto createRegistration(RegistrationRequestDto requestDto) {
		Event event = eventRepository.findById(requestDto.getEventId())
			.orElseThrow(() -> new ResourceNotFoundException("Event not found"));

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (event.getCreator() != null && event.getCreator().getId() == user.getId()) {
			throw new BadRequestException("Creators cannot register to their own events");
		}

		if (event.getStatus() != EventStatus.PUBLISHED) {
			throw new BadRequestException("Cannot register to an event that is not published");
		}

		long confirmedCount = countConfirmedParticipants(event);
		if (isFull(event, confirmedCount)) {
			event.setStatus(EventStatus.COMPLETED);
			eventRepository.save(event);
			throw new BadRequestException("Event is full");
		}

		Registration existing = registrationRepository.findByEventAndUser(event, user).orElse(null);
		if (existing != null) {
			if (existing.getStatus() != RegistrationStatus.CANCELLED) {
				return new RegistrationResponseDto(existing.getId(), existing.getStatus(), existing.getEvent().getId(), existing.getUser().getId());
			}

			existing.setStatus(RegistrationStatus.CONFIRMED);
			Registration savedExisting = registrationRepository.save(existing);
			completeEventIfFull(event, confirmedCount + 1);
			return new RegistrationResponseDto(savedExisting.getId(), savedExisting.getStatus(), savedExisting.getEvent().getId(), savedExisting.getUser().getId());
		}

		Registration registration = new Registration(RegistrationStatus.CONFIRMED, event, user);
		Registration saved = registrationRepository.save(registration);
		completeEventIfFull(event, confirmedCount + 1);

		return new RegistrationResponseDto(saved.getId(), saved.getStatus(), saved.getEvent().getId(), saved.getUser().getId());
	}

	private RegistrationResponseDto toResponseDto(Registration reg) {
		return new RegistrationResponseDto(
				reg.getId(),
				reg.getStatus(),
				reg.getEvent() != null ? reg.getEvent().getId() : null,
				reg.getUser() != null ? reg.getUser().getId() : null
		);
	}

	private void completeEventIfFull(Event event, long registeredCount) {
		if (isFull(event, registeredCount)) {
			event.setStatus(EventStatus.COMPLETED);
			eventRepository.save(event);
		}
	}

	private void reopenEventIfSeatFreed(Event event) {
		if (event == null || event.getStatus() != EventStatus.COMPLETED || event.getCapacityMax() == null) {
			return;
		}

		long registeredCount = countConfirmedParticipants(event);
		if (!isFull(event, registeredCount)) {
			event.setStatus(EventStatus.PUBLISHED);
			eventRepository.save(event);
		}
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
