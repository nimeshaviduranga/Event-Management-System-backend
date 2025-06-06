package com.eventmanagement.repository;

import com.eventmanagement.entity.User;
import com.eventmanagement.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    //Find user by email
    Optional<User> findByEmail(String email);

    //Check if email exists
    boolean existsByEmail(String email);

    //Find user by Role
    List<User> findByRole(Role role);

    //FInd user by role with pagination
    Page<User> findByRole(Role role, Pageable pageable);

}