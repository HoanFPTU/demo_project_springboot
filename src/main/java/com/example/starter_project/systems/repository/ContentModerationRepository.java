package com.example.starter_project.systems.repository;

import com.example.starter_project.systems.entity.ContentModeration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContentModerationRepository extends JpaRepository<ContentModeration, Long> {

    Optional<ContentModeration> findByContentName(String contentName);

    Page<ContentModeration> findByContentNameContainingIgnoreCase(String keyword, Pageable pageable);

    long countByContentNameContainingIgnoreCase(String keyword);
}
