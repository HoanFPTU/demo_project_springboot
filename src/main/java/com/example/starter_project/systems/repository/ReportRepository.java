package com.example.starter_project.systems.repository;

import com.example.starter_project.systems.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByName(String name);
    Page<Report> findByNameContainingIgnoreCase(String name, Pageable pageable);
    long countByNameContainingIgnoreCase(String name);
}