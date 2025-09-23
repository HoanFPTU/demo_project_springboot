package com.example.starter_project.systems.repository;

import com.example.starter_project.systems.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    Optional<Playlist> findByName(String name);
    Page<Playlist> findByNameContainingIgnoreCase(String name, Pageable pageable);
    long countByNameContainingIgnoreCase(String name);
}
