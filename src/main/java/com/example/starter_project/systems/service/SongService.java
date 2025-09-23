package com.example.starter_project.systems.service;

import com.example.starter_project.systems.dto.SongDTO;
import com.example.starter_project.systems.entity.Song;
import com.example.starter_project.systems.mapper.SongMapper;
import com.example.starter_project.systems.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class SongService {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private SongMapper songMapper;

    private String validateSortBy(String sortBy) {
        List<String> allowedSortFields = List.of("id", "name");
        return allowedSortFields.contains(sortBy) ? sortBy : "name";
    }

    public Optional<Song> findById(Long id) {
        return songRepository.findById(id);
    }

    public Optional<Song> findByName(String name) {
        return songRepository.findByName(name);
    }

    public List<SongDTO> findAll() {
        List<Song> songs = songRepository.findAll();
        return songMapper.toDTOList(songs);
    }

    public Page<SongDTO> findAllPaged(int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Song> songs = songRepository.findAll(pageable);
        return songs.map(songMapper::toDTO);
    }

    public Page<SongDTO> searchSongs(String query, int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Song> songs = songRepository.findByNameContainingIgnoreCase(query, pageable);
        return songs.map(songMapper::toDTO);
    }

    public SongDTO create(SongDTO dto) {
        Song entity = songMapper.toEntity(dto);
        Song saved = songRepository.save(entity);
        return songMapper.toDTO(saved);
    }

    public SongDTO update(Long id, SongDTO dto) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Song not found"));
        songMapper.updateEntityFromDto(dto, song);
        Song updated = songRepository.save(song);
        return songMapper.toDTO(updated);
    }

    public void deleteSong(Long id) {
        songRepository.deleteById(id);
    }

    public long countSongs() {
        return songRepository.count();
    }

    public long countBySearchCriteria(String search) {
        return songRepository.countByNameContainingIgnoreCase(search);
    }
}
