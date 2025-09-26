package com.example.starter_project.systems.controller;

import com.example.starter_project.systems.dto.ContentModerationDTO;
import com.example.starter_project.systems.service.ContentModerationService; // ✅ thêm import
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
    public ResponseEntity<?> create(@RequestBody @Valid ContentModerationDTO dto) {
        if (service.findByContentName(dto.getContentName()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("ContentModeration with name '" + dto.getContentName() + "' already exists");
        }
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid ContentModerationDTO dto) {
        if (!service.findById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("ContentModeration with id '" + id + "' not found");
        }
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!service.findById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("ContentModeration with id '" + id + "' not found");
        }
        service.delete(id);
        return ResponseEntity.ok("Deleted successfully");
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
