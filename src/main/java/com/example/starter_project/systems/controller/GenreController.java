package com.example.starter_project.systems.controller;

import com.example.starter_project.systems.dto.GenreDTO;
import com.example.starter_project.systems.entity.Genre;
import com.example.starter_project.systems.service.GenreService;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/genres")
public class GenreController {

    @Autowired
    private GenreService genreService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid GenreDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Genre name cannot be null or empty.");
        }
        Optional<Genre> existingGenre = genreService.findByName(dto.getName());
        if (existingGenre.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("Error: Genre with name '%s' already exists.", dto.getName()));
        }
        try {
            GenreDTO createdGenre = genreService.create(dto);
            return ResponseEntity.ok(createdGenre);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to create genre due to internal error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<GenreDTO>> getAll(
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
            return ResponseEntity.ok(genreService.searchGenres(search, page, size, sortBy, sortDirection));
        }
        return ResponseEntity.ok(genreService.findAllPaged(page, size, sortBy, sortDirection));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid GenreDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Genre name cannot be null or empty.");
        }
        Optional<Genre> genreById = genreService.findById(id);
        if (!genreById.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("Error: Genre with ID '%d' not found.", id));
        }
        Optional<Genre> existingGenre = genreService.findByName(dto.getName());
        if (existingGenre.isPresent() && !existingGenre.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("Error: Genre with name '%s' already exists.", dto.getName()));
        }
        try {
            GenreDTO updatedGenre = genreService.update(id, dto);
            return ResponseEntity.ok(updatedGenre);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to update genre due to internal error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> errors = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String name = row.getCell(0) != null ? row.getCell(0).getStringCellValue() : null;
                if (name == null || name.trim().isEmpty()) {
                    errors.add(String.format("Row %d: Genre name cannot be null or empty.", i + 1));
                    continue;
                }
                Optional<Genre> existingGenre = genreService.findByName(name);
                if (existingGenre.isPresent()) {
                    errors.add(String.format("Row %d: Genre with name '%s' already exists.", i + 1, name));
                    continue;
                }
                GenreDTO dto = new GenreDTO();
                dto.setName(name);
                genreService.create(dto);
            }
            if (!errors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Import failed with the following errors: " + String.join("; ", errors));
            }
            return ResponseEntity.ok("Genres imported successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error importing genres: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=genres.xlsx");
        List<GenreDTO> genres = genreService.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Genres");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");

        for (int i = 0; i < genres.size(); i++) {
            GenreDTO genre = genres.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(genre.getName());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getGenreCount(@RequestParam(required = false) String search) {
        long count = (search != null && !search.isEmpty()) ?
                genreService.countBySearchCriteria(search) : genreService.countGenres();
        return ResponseEntity.ok(count);
    }
}