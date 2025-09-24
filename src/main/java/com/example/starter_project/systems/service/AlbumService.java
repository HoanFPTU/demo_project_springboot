package com.example.starter_project.systems.service;

import com.example.starter_project.systems.dto.AlbumDTO;
import com.example.starter_project.systems.entity.Album;
import com.example.starter_project.systems.mapper.AlbumMapper;
import com.example.starter_project.systems.repository.AlbumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlbumService {

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private AlbumMapper albumMapper;

    private String validateSortBy(String sortBy) {
        List<String> allowedSortFields = List.of("id", "name");
        return allowedSortFields.contains(sortBy) ? sortBy : "name";
    }

    public Optional<Album> findById(Long id) {
        return albumRepository.findById(id);
    }

    public Optional<Album> findByName(String name) {
        return albumRepository.findByName(name);
    }

    public List<AlbumDTO> findAll() {
        List<Album> albums = albumRepository.findAll();
        return albumMapper.toDTOList(albums);
    }

    public Page<AlbumDTO> findAllPaged(int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Album> albums = albumRepository.findAll(pageable);
        return albums.map(albumMapper::toDTO);
    }

    public Page<AlbumDTO> searchAlbums(String query, int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Album> albums = albumRepository.findByNameContainingIgnoreCase(query, pageable);
        return albums.map(albumMapper::toDTO);
    }

    public AlbumDTO create(AlbumDTO dto) {
        Album entity = albumMapper.toEntity(dto);
        Album saved = albumRepository.save(entity);
        return albumMapper.toDTO(saved);
    }

    public AlbumDTO update(Long id, AlbumDTO dto) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Album not found"));
        albumMapper.updateEntityFromDto(dto, album);
        Album updated = albumRepository.save(album);
        return albumMapper.toDTO(updated);
    }

    public void deleteAlbum(Long id) {
        albumRepository.deleteById(id);
    }

    public long countAlbums() {
        return albumRepository.count();
    }

    public long countBySearchCriteria(String search) {
        return albumRepository.countByNameContainingIgnoreCase(search);
    }
}
