package com.example.starter_project.systems.repository;

import com.example.starter_project.systems.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    Page<Role> findByNameContainingIgnoreCase(String name, Pageable pageable);
    long countByNameContainingIgnoreCase(String name);
}