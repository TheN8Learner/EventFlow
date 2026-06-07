package com.eventflow.eventflow.repository;

import com.eventflow.eventflow.model.UserEventRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEventRoleRepository extends JpaRepository<UserEventRole, Long> {
}
