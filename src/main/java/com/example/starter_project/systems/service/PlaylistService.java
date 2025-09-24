package com.example.starter_project.systems.service;

import com.example.starter_project.systems.dto.PlaylistDTO;
import com.example.starter_project.systems.entity.Playlist;
import com.example.starter_project.systems.mapper.PlaylistMapper;
import com.example.starter_project.systems.repository.PlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistMapper playlistMapper;

    private String validateSortBy(String sortBy) {
        List<String> allowedSortFields = List.of("id", "name");
        return allowedSortFields.contains(sortBy) ? sortBy : "name";
    }

    public Optional<Playlist> findById(Long id) {
        return playlistRepository.findById(id);
    }

    public Optional<Playlist> findByName(String name) {
        return playlistRepository.findByName(name);
    }

    public List<PlaylistDTO> findAll() {
        List<Playlist> playlists = playlistRepository.findAll();
        return playlistMapper.toDTOList(playlists);
    }

    public Page<PlaylistDTO> findAllPaged(int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Playlist> playlists = playlistRepository.findAll(pageable);
        return playlists.map(playlistMapper::toDTO);
    }

    public Page<PlaylistDTO> searchPlaylists(String query, int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Playlist> playlists = playlistRepository.findByNameContainingIgnoreCase(query, pageable);
        return playlists.map(playlistMapper::toDTO);
    }

    public PlaylistDTO create(PlaylistDTO dto) {
        Playlist entity = playlistMapper.toEntity(dto);
        Playlist saved = playlistRepository.save(entity);
        return playlistMapper.toDTO(saved);
    }

    public PlaylistDTO update(Long id, PlaylistDTO dto) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));
        playlistMapper.updateEntityFromDto(dto, playlist);
        Playlist updated = playlistRepository.save(playlist);
        return playlistMapper.toDTO(updated);
    }

    public void deletePlaylist(Long id) {
        playlistRepository.deleteById(id);
    }

    public long countPlaylists() {
        return playlistRepository.count();
    }

    public long countBySearchCriteria(String search) {
        return playlistRepository.countByNameContainingIgnoreCase(search);
    }

    public List<Playlist> findAllEntities() {
        return playlistRepository.findAll();
    }

}