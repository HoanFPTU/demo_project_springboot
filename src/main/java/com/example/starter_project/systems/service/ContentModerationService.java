package com.example.starter_project.systems.service;

import com.example.starter_project.systems.dto.ContentModerationDTO;
import com.example.starter_project.systems.entity.ContentModeration;
import com.example.starter_project.systems.mapper.ContentModerationMapper;
import com.example.starter_project.systems.repository.ContentModerationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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

    private Sort.Direction validateSortDirection(String sortDirection) {
        try {
            return Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException ex) {
            return Sort.Direction.ASC;
        }
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
        Sort.Direction direction = validateSortDirection(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return contentModerationRepository.findAll(pageable).map(contentModerationMapper::toDTO);
    }

    public Page<ContentModerationDTO> search(String keyword, int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort.Direction direction = validateSortDirection(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return contentModerationRepository.findByContentNameContainingIgnoreCase(keyword, pageable)
                .map(contentModerationMapper::toDTO);
    }

    @Transactional
    public ContentModerationDTO create(ContentModerationDTO dto) {
        // check duplicate name
        contentModerationRepository.findByContentName(dto.getContentName()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ContentModeration with name '" + dto.getContentName() + "' already exists");
        });

        ContentModeration entity = contentModerationMapper.toEntity(dto);
        ContentModeration saved = contentModerationRepository.save(entity);
        return contentModerationMapper.toDTO(saved);
    }

    @Transactional
    public ContentModerationDTO update(Long id, ContentModerationDTO dto) {
        ContentModeration entity = contentModerationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ContentModeration with id '" + id + "' not found"));

        // If contentName is being changed, ensure uniqueness
        if (dto.getContentName() != null && !dto.getContentName().equals(entity.getContentName())) {
            contentModerationRepository.findByContentName(dto.getContentName()).ifPresent(conflict -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "ContentModeration with name '" + dto.getContentName() + "' already exists");
            });
        }

        contentModerationMapper.updateEntityFromDto(dto, entity);
        ContentModeration saved = contentModerationRepository.save(entity);
        return contentModerationMapper.toDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!contentModerationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ContentModeration with id '" + id + "' not found");
        }
        contentModerationRepository.deleteById(id);
    }

    public long countContentModerations() {
        return contentModerationRepository.count();
    }

    public long countBySearchCriteria(String keyword) {
        return contentModerationRepository.countByContentNameContainingIgnoreCase(keyword);
    }
}
