package com.example.starter_project.systems.service;

import com.example.starter_project.systems.dto.GenreDTO;
import com.example.starter_project.systems.entity.Genre;
import com.example.starter_project.systems.mapper.GenreMapper;
import com.example.starter_project.systems.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private GenreMapper genreMapper;

    private String validateSortBy(String sortBy) {
        List<String> allowedSortFields = List.of("id", "name");
        return allowedSortFields.contains(sortBy) ? sortBy : "name";
    }

    public Optional<Genre> findById(Long id) {
        return genreRepository.findById(id);
    }

    public Optional<Genre> findByName(String name) {
        return genreRepository.findByName(name);
    }

    public List<GenreDTO> findAll() {
        List<Genre> genres = genreRepository.findAll();
        return genreMapper.toDTOList(genres);
    }

    public Page<GenreDTO> findAllPaged(int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Genre> genres = genreRepository.findAll(pageable);
        return genres.map(genreMapper::toDTO);
    }

    public Page<GenreDTO> searchGenres(String query, int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Genre> genres = genreRepository.findByNameContainingIgnoreCase(query, pageable);
        return genres.map(genreMapper::toDTO);
    }

    public GenreDTO create(GenreDTO dto) {
        Genre entity = genreMapper.toEntity(dto);
        Genre saved = genreRepository.save(entity);
        return genreMapper.toDTO(saved);
    }

    public GenreDTO update(Long id, GenreDTO dto) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found"));
        genreMapper.updateEntityFromDto(dto, genre);
        Genre updated = genreRepository.save(genre);
        return genreMapper.toDTO(updated);
    }

    public void deleteGenre(Long id) {
        genreRepository.deleteById(id);
    }

    public long countGenres() {
        return genreRepository.count();
    }

    public long countBySearchCriteria(String search) {
        return genreRepository.countByNameContainingIgnoreCase(search);
    }
}