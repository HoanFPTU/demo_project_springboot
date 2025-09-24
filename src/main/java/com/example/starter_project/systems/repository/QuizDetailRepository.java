package com.example.starter_project.systems.repository;

import com.example.starter_project.systems.entity.QuizDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface QuizDetailRepository extends JpaRepository<QuizDetail, Long> {
    Optional<QuizDetail> findByName(String name);
    Page<QuizDetail> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
