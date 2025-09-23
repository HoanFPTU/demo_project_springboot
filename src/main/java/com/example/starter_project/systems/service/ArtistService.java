package com.example.starter_project.systems.service;

import com.example.starter_project.systems.dto.ArtistDTO;
import com.example.starter_project.systems.entity.Artist;
import com.example.starter_project.systems.mapper.ArtistMapper;
import com.example.starter_project.systems.repository.ArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class ArtistService {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private ArtistMapper artistMapper;

    private String validateSortBy(String sortBy) {
        List<String> allowedSortFields = List.of("id", "name");
        return allowedSortFields.contains(sortBy) ? sortBy : "name";
    }

    public Optional<Artist> findById(Long id) {
        return artistRepository.findById(id);
    }

    public Optional<Artist> findByName(String name) {
        return artistRepository.findByName(name);
    }

    public List<ArtistDTO> findAll() {
        List<Artist> artists = artistRepository.findAll();
        return artistMapper.toDTOList(artists);
    }

    public Page<ArtistDTO> findAllPaged(int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Artist> artists = artistRepository.findAll(pageable);
        return artists.map(artistMapper::toDTO);
    }

    public Page<ArtistDTO> searchArtists(String query, int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Artist> artists = artistRepository.findByNameContainingIgnoreCase(query, pageable);
        return artists.map(artistMapper::toDTO);
    }

    public ArtistDTO create(ArtistDTO dto) {
        Artist entity = artistMapper.toEntity(dto);
        Artist saved = artistRepository.save(entity);
        return artistMapper.toDTO(saved);
    }

    public ArtistDTO update(Long id, ArtistDTO dto) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artist not found"));
        artistMapper.updateEntityFromDto(dto, artist);
        Artist updated = artistRepository.save(artist);
        return artistMapper.toDTO(updated);
    }

    public void deleteArtist(Long id) {
        artistRepository.deleteById(id);
    }

    public long countArtists() {
        return artistRepository.count();
    }

    public long countBySearchCriteria(String search) {
        return artistRepository.countByNameContainingIgnoreCase(search);
    }
}
