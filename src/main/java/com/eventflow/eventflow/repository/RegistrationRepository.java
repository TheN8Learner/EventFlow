package com.eventflow.eventflow.repository;

import com.eventflow.eventflow.model.Event;
import com.eventflow.eventflow.model.Registration;
import com.eventflow.eventflow.model.RegistrationStatus;
import com.eventflow.eventflow.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
	long countByEventAndStatus(Event event, RegistrationStatus status);
	long countByEventAndStatusAndUserNot(Event event, RegistrationStatus status, User user);
	Page<Registration> findAll(Pageable pageable);
	boolean existsByEventAndUser(Event event, User user);
	Optional<Registration> findByEventAndUser(Event event, User user);
	Page<Registration> findByUser(User user, Pageable pageable);
	Page<Registration> findByEvent(Event event, Pageable pageable);
	Page<Registration> findByEventIn(List<Event> events, Pageable pageable);
	Page<Registration> findByEventInAndUserNot(List<Event> events, User user, Pageable pageable);
	Page<Registration> findByEventAndUserNot(Event event, User user, Pageable pageable);
	Page<Registration> findByEventAndStatus(Event event, RegistrationStatus status, Pageable pageable);
	Page<Registration> findByUserAndStatus(User user, RegistrationStatus status, Pageable pageable);
	Page<Registration> findByUserAndStatusNot(User user, RegistrationStatus status, Pageable pageable);
}
