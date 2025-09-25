package com.example.starter_project.systems.repository;

import com.example.starter_project.systems.entity.Podcast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PodcastRepository  extends JpaRepository<Podcast,Long> {
    Optional<Podcast> findByName(String name);
    Page<Podcast> findByNameContainingIgnoreCase(String name, Pageable pageable);
    long countByNameContainingIgnoreCase(String name);
}
