package com.example.starter_project.systems.controller;

import com.example.starter_project.systems.dto.PodcastDTO;
import com.example.starter_project.systems.entity.Podcast;
import com.example.starter_project.systems.service.PodcastService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/podcasts")
public class PodcastController {
    @Autowired
    private PodcastService podcastService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid PodcastDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Podcast name cannot be null or empty.");
        }
        Optional<Podcast> existingPodcast = podcastService.findByName(dto.getName());
        if (existingPodcast.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("Error: Podcast with name '%s' already exists.", dto.getName()));
        }
        try {
            PodcastDTO createdPodcast = podcastService.create(dto);
            return ResponseEntity.ok(createdPodcast);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to create podcast due to internal error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<PodcastDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            @RequestParam(required = false) String search
    ) {
        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "asc";

        // ✅ Whitelist allowed sort fields for safety
        List<String> allowedSortFields = List.of("id", "name");
        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "name";
        }

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(podcastService.searchPodcasts(search, page, size, sortBy, sortDirection));
        }
        return ResponseEntity.ok(podcastService.findAllPaged(page, size, sortBy, sortDirection));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid PodcastDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Podcast name cannot be null or empty.");
        }
        Optional<Podcast> podcastById = podcastService.findById(id);
        if (!podcastById.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("Error: Podcast with ID '%d' not found.", id));
        }
        Optional<Podcast> existingPodcast = podcastService.findByName(dto.getName());
        if (existingPodcast.isPresent() && !existingPodcast.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("Error: Podcast with name '%s' already exists.", dto.getName()));
        }
        try {
            PodcastDTO updatedPodcast = podcastService.update(id, dto);
            return ResponseEntity.ok(updatedPodcast);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to update podcast due to internal error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        podcastService.deletePodcast(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> errors = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String name = row.getCell(0) != null ? row.getCell(0).getStringCellValue() : null;
                if (name == null || name.trim().isEmpty()) {
                    errors.add(String.format("Row %d: Podcast name cannot be null or empty.", i + 1));
                    continue;
                }
                Optional<Podcast> existingPodcast = podcastService.findByName(name);
                if (existingPodcast.isPresent()) {
                    errors.add(String.format("Row %d: Podcast with name '%s' already exists.", i + 1, name));
                    continue;
                }
                PodcastDTO dto = new PodcastDTO();
                dto.setName(name);
                podcastService.create(dto);
            }
            if (!errors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Import failed with the following errors: " + String.join("; ", errors));
            }
            return ResponseEntity.ok("Podcasts imported successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error importing podcasts: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=podcasts.xlsx");
        List<PodcastDTO> podcasts = podcastService.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Podcasts");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");

        for (int i = 0; i < podcasts.size(); i++) {
            PodcastDTO podcast = podcasts.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(podcast.getName());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getPodcastCount(@RequestParam(required = false) String search) {
        long count = (search != null && !search.isEmpty()) ?
                podcastService.countBySearchCriteria(search) : podcastService.countPodcasts();
        return ResponseEntity.ok(count);
    }
    
    
}
