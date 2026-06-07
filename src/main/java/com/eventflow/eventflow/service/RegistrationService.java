package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.RegistrationRequestDto;
import com.eventflow.eventflow.dto.RegistrationResponseDto;
import com.eventflow.eventflow.model.*;
import com.eventflow.eventflow.repository.EventRepository;
import com.eventflow.eventflow.repository.RegistrationRepository;
import com.eventflow.eventflow.repository.UserRepository;
import com.eventflow.eventflow.exceptions.ResourceNotFoundException;
import com.eventflow.eventflow.exceptions.BadRequestException;
import com.eventflow.eventflow.exceptions.ConflictException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

	public Page<RegistrationResponseDto> getRegistrations(Pageable pageable) {
		Page<Registration> registrations = registrationRepository.findAll(pageable);
		return registrations.map(reg -> new RegistrationResponseDto(
				reg.getId(),
				reg.getStatus(),
				reg.getEvent() != null ? reg.getEvent().getId() : null,
				reg.getUser() != null ? reg.getUser().getId() : null
		));
	}

	public RegistrationResponseDto createRegistration(RegistrationRequestDto requestDto) {
		Event event = eventRepository.findById(requestDto.getEventId())
			.orElseThrow(() -> new ResourceNotFoundException("Event not found"));
		// prevent registrations to cancelled or finished events
		if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.FINISHED) {
		    throw new BadRequestException("Cannot register to cancelled or completed event");
		}

		String email = SecurityContextHolder.getContext().getAuthentication().getName();

		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		// prevent duplicate registrations
		if (registrationRepository.existsByEventAndUser(event, user)) {
			throw new ConflictException("User already registered for this event");
		}

		long confirmedCount = registrationRepository.countByEventAndStatus(event, RegistrationStatus.CONFIRMED);

		RegistrationStatus status = RegistrationStatus.WAITLISTED;
		if (event.getCapacityMax() == null || confirmedCount < event.getCapacityMax()) {
			status = RegistrationStatus.CONFIRMED;
		}

		Registration registration = new Registration(status, event, user);
		Registration saved = registrationRepository.save(registration);

		return new RegistrationResponseDto(saved.getId(), saved.getStatus(), saved.getEvent().getId(), saved.getUser().getId());
	}
}
