package com.eventflow.eventflow.repository;

import com.eventflow.eventflow.model.Event;
import com.eventflow.eventflow.model.Registration;
import com.eventflow.eventflow.model.RegistrationStatus;
import com.eventflow.eventflow.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
	long countByEventAndStatus(Event event, RegistrationStatus status);
	Page<Registration> findAll(Pageable pageable);
	boolean existsByEventAndUser(Event event, User user);
	Page<Registration> findByUser(User user, Pageable pageable);
	Page<Registration> findByEvent(Event event, Pageable pageable);
	Page<Registration> findByEventAndStatus(Event event, RegistrationStatus status, Pageable pageable);
	Page<Registration> findByUserAndStatus(User user, RegistrationStatus status, Pageable pageable);
}
