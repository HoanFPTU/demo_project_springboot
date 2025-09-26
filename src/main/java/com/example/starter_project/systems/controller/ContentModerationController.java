package com.example.starter_project.systems.controller;

import com.example.starter_project.systems.dto.ContentModerationDTO;
import com.example.starter_project.systems.service.ContentModerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/content-moderations")
@RequiredArgsConstructor
public class ContentModerationController {

    private final ContentModerationService service;

    @PostMapping
    public ResponseEntity<ContentModerationDTO> create(@RequestBody @Valid ContentModerationDTO dto) {
        ContentModerationDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContentModerationDTO> update(@PathVariable Long id, @RequestBody @Valid ContentModerationDTO dto) {
        ContentModerationDTO updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ContentModerationDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return ResponseEntity.ok(service.findAllPaged(page, size, sortBy, sortDirection));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ContentModerationDTO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        return ResponseEntity.ok(service.search(keyword, page, size, sortBy, sortDirection));
    }
}
