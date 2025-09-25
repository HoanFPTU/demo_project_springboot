package com.example.starter_project.systems.service;

import com.example.starter_project.systems.dto.PodcastDTO;
import com.example.starter_project.systems.entity.Podcast;
import com.example.starter_project.systems.mapper.PodcastMapper;
import com.example.starter_project.systems.repository.PodcastRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PodcastService {
    @Autowired
    private PodcastRepository podcastRepository;

    @Autowired
    private PodcastMapper  podcastMapper;

    private String validateSortBy(String sortBy) {
        List<String> allowedSortFields = List.of("id", "name");
        return allowedSortFields.contains(sortBy) ? sortBy : "name";
    }

    public Optional<Podcast> findById(Long id) {
        return podcastRepository.findById(id);
    }

    public Optional<Podcast> findByName(String name) {
        return podcastRepository.findByName(name);
    }

    public List<PodcastDTO> findAll() {
        List<Podcast> podcasts = podcastRepository.findAll();
        return podcastMapper.toDTOList(podcasts);
    }

    public Page<PodcastDTO> findAllPaged(int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Podcast> podcasts = podcastRepository.findAll(pageable);
        return podcasts.map(podcastMapper::toDTO);
    }

    public Page<PodcastDTO> searchPodcasts(String query, int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Podcast> podcasts = podcastRepository.findByNameContainingIgnoreCase(query, pageable);
        return podcasts.map(podcastMapper::toDTO);
    }

    public PodcastDTO create(PodcastDTO dto) {
        Podcast entity = podcastMapper.toEntity(dto);
        Podcast saved = podcastRepository.save(entity);
        return podcastMapper.toDTO(saved);
    }

    public PodcastDTO update(Long id, PodcastDTO dto) {
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Podcast not found"));
        podcastMapper.updateEntityFromDto(dto, podcast);
        Podcast updated = podcastRepository.save(podcast);
        return podcastMapper.toDTO(updated);
    }

    public void deletePodcast(Long id) {
        podcastRepository.deleteById(id);
    }

    public long countPodcasts() {
        return podcastRepository.count();
    }

    public long countBySearchCriteria(String search) {
        return podcastRepository.countByNameContainingIgnoreCase(search);
    }

}
