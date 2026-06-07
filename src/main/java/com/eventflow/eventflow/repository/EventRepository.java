package com.eventflow.eventflow.repository;

import com.eventflow.eventflow.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.eventflow.eventflow.model.EventStatus;
import com.eventflow.eventflow.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
	Page<Event> findByStatus(EventStatus status, Pageable pageable);
	Page<Event> findByCreator(User creator, Pageable pageable);
}
