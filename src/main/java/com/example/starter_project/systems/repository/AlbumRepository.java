package com.example.starter_project.systems.repository;

import com.example.starter_project.systems.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    Optional<Album> findByName(String name);
    Page<Album> findByNameContainingIgnoreCase(String name, Pageable pageable);
    long countByNameContainingIgnoreCase(String name);
}
