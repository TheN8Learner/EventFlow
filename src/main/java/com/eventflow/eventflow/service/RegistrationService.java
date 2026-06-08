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
import org.springframework.transaction.annotation.Transactional;

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

	public Page<RegistrationResponseDto> getRegistrationsForMyCreatedEvent(Long eventId, RegistrationStatus status, Pageable pageable) {
		Event event = eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event not found"));
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (event.getCreator() == null || event.getCreator().getId() != user.getId()) {
			throw new BadRequestException("Not authorized to view registrations for this event");
		}

		if (status != null) {
			return registrationRepository.findByEventAndStatusAndUserNot(event, status, user, pageable)
					.map(this::toResponseDto);
		}

		return registrationRepository.findByEventAndUserNot(event, user, pageable)
				.map(this::toResponseDto);
	}

	@Transactional
	public RegistrationResponseDto cancelMyRegistration(Long registrationId) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Registration reg = registrationRepository.findById(registrationId)
				.orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

		if (reg.getUser() == null || reg.getUser().getId() != user.getId()) {
			throw new BadRequestException("Cannot cancel another user's registration");
		}

		RegistrationStatus previousStatus = reg.getStatus();
		reg.setStatus(RegistrationStatus.CANCELLED);
		registrationRepository.save(reg);

		if (previousStatus == RegistrationStatus.CONFIRMED) {
			promoteNextWaitlistedOrReopen(reg.getEvent());
		}

		return toResponseDto(reg);
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

	@Transactional
	public RegistrationResponseDto createRegistration(RegistrationRequestDto requestDto) {
		Event event = eventRepository.findById(requestDto.getEventId())
			.orElseThrow(() -> new ResourceNotFoundException("Event not found"));

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (event.getCreator() != null && event.getCreator().getId() == user.getId()) {
			throw new BadRequestException("Creators cannot register to their own events");
		}

		if (event.getStatus() != EventStatus.PUBLISHED && event.getStatus() != EventStatus.COMPLETED) {
			throw new BadRequestException("Cannot register to an event that is not open for registrations");
		}

		long confirmedCount = countConfirmedParticipants(event);
		boolean full = isFull(event, confirmedCount);
		RegistrationStatus targetStatus = full ? RegistrationStatus.WAITLISTED : RegistrationStatus.CONFIRMED;
		if (full && event.getStatus() != EventStatus.COMPLETED) {
			event.setStatus(EventStatus.COMPLETED);
			event = eventRepository.save(event);
		}

		Registration existing = registrationRepository.findByEventAndUser(event, user).orElse(null);
		if (existing != null) {
			if (existing.getStatus() != RegistrationStatus.CANCELLED) {
				return toResponseDto(existing);
			}

			existing.setStatus(targetStatus);
			Registration savedExisting = registrationRepository.save(existing);
			if (targetStatus == RegistrationStatus.CONFIRMED) {
				completeEventIfFull(event, confirmedCount + 1);
			}
			return toResponseDto(savedExisting);
		}

		Registration registration = new Registration(targetStatus, event, user);
		Registration saved = registrationRepository.save(registration);
		if (targetStatus == RegistrationStatus.CONFIRMED) {
			completeEventIfFull(event, confirmedCount + 1);
		}

		return toResponseDto(saved);
	}

	private RegistrationResponseDto toResponseDto(Registration reg) {
		RegistrationResponseDto dto = new RegistrationResponseDto(
				reg.getId(),
				reg.getStatus(),
				reg.getEvent() != null ? reg.getEvent().getId() : null,
				reg.getUser() != null ? reg.getUser().getId() : null
		);
		if (reg.getUser() != null) {
			String firstName = reg.getUser().getFirstName() == null ? "" : reg.getUser().getFirstName();
			String lastName = reg.getUser().getLastName() == null ? "" : reg.getUser().getLastName();
			dto.setUserName((firstName + " " + lastName).trim());
			dto.setUserEmail(reg.getUser().getEmail());
		}
		if (reg.getEvent() != null) {
			dto.setEventTitle(reg.getEvent().getTitle());
		}
		return dto;
	}

	private void completeEventIfFull(Event event, long registeredCount) {
		if (isFull(event, registeredCount)) {
			event.setStatus(EventStatus.COMPLETED);
			eventRepository.save(event);
		}
	}

	private void promoteNextWaitlistedOrReopen(Event event) {
		if (event == null || event.getCapacityMax() == null) {
			return;
		}

		Registration nextWaitlisted = registrationRepository
				.findFirstByEventAndStatusOrderByIdAsc(event, RegistrationStatus.WAITLISTED)
				.orElse(null);

		if (nextWaitlisted != null) {
			nextWaitlisted.setStatus(RegistrationStatus.CONFIRMED);
			registrationRepository.save(nextWaitlisted);
			event.setStatus(EventStatus.COMPLETED);
			eventRepository.save(event);
			return;
		}

		if (event.getStatus() != EventStatus.COMPLETED) {
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
