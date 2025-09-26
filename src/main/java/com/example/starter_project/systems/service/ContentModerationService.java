package com.example.starter_project.systems.service;

import com.example.starter_project.systems.dto.ContentModerationDTO;
import com.example.starter_project.systems.entity.ContentModeration;
import com.example.starter_project.systems.mapper.ContentModerationMapper;
import com.example.starter_project.systems.repository.ContentModerationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContentModerationService {

    private final ContentModerationRepository contentModerationRepository;
    private final ContentModerationMapper contentModerationMapper;

    private String validateSortBy(String sortBy) {
        List<String> allowedSortFields = List.of("id", "contentName", "status");
        return allowedSortFields.contains(sortBy) ? sortBy : "id";
    }

    public Optional<ContentModeration> findById(Long id) {
        return contentModerationRepository.findById(id);
    }

    public Optional<ContentModeration> findByContentName(String contentName) {
        return contentModerationRepository.findByContentName(contentName);
    }

    public List<ContentModerationDTO> findAll() {
        return contentModerationMapper.toDTOList(contentModerationRepository.findAll());
    }

    public Page<ContentModerationDTO> findAllPaged(int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        return contentModerationRepository.findAll(pageable).map(contentModerationMapper::toDTO);
    }

    public Page<ContentModerationDTO> search(String keyword, int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        return contentModerationRepository.findByContentNameContainingIgnoreCase(keyword, pageable)
                .map(contentModerationMapper::toDTO);
    }

    public ContentModerationDTO create(ContentModerationDTO dto) {
        ContentModeration entity = contentModerationMapper.toEntity(dto);
        return contentModerationMapper.toDTO(contentModerationRepository.save(entity));
    }

    public ContentModerationDTO update(Long id, ContentModerationDTO dto) {
        ContentModeration entity = contentModerationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ContentModeration not found"));
        contentModerationMapper.updateEntityFromDto(dto, entity);
        return contentModerationMapper.toDTO(contentModerationRepository.save(entity));
    }

    public void delete(Long id) {
        contentModerationRepository.deleteById(id);
    }

    public long countContentModerations() {
        return contentModerationRepository.count();
    }

    public long countBySearchCriteria(String keyword) {
        return contentModerationRepository.countByContentNameContainingIgnoreCase(keyword);
    }
}
