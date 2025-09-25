package com.example.starter_project.systems.controller;

import com.example.starter_project.systems.dto.ArtistDTO;
import com.example.starter_project.systems.entity.Artist;
import com.example.starter_project.systems.service.ArtistService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.CellType;
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
@RequestMapping("/api/artists")
public class ArtistController {

    @Autowired
    private ArtistService artistService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid ArtistDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Artist name cannot be null or empty.");
        }
        Optional<Artist> existingArtist = artistService.findByName(dto.getName());
        if (existingArtist.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("Error: Artist with name '%s' already exists.", dto.getName()));
        }
        try {
            ArtistDTO createdArtist = artistService.create(dto);
            return ResponseEntity.ok(createdArtist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to create artist due to internal error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<ArtistDTO>> getAll(
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
            return ResponseEntity.ok(artistService.searchArtists(search, page, size, sortBy, sortDirection));
        }
        return ResponseEntity.ok(artistService.findAllPaged(page, size, sortBy, sortDirection));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid ArtistDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Artist name cannot be null or empty.");
        }
        Optional<Artist> artistById = artistService.findById(id);
        if (!artistById.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("Error: Artist with ID '%d' not found.", id));
        }
        Optional<Artist> existingArtist = artistService.findByName(dto.getName());
        if (existingArtist.isPresent() && !existingArtist.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("Error: Artist with name '%s' already exists.", dto.getName()));
        }
        try {
            ArtistDTO updatedArtist = artistService.update(id, dto);
            return ResponseEntity.ok(updatedArtist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to update artist due to internal error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        artistService.deleteArtist(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> errors = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Bỏ row header
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String name = row.getCell(0) != null ? row.getCell(0).getStringCellValue().trim() : null;
                String country = row.getCell(1) != null ? row.getCell(1).getStringCellValue().trim() : null;

                Integer debutYear = null;
                if (row.getCell(2) != null) {
                    if (row.getCell(2).getCellType() == CellType.NUMERIC) {
                        debutYear = (int) row.getCell(2).getNumericCellValue();
                    } else {
                        errors.add(String.format("Row %d: DebutYear must be a number.", i + 1));
                    }
                }

                String description = row.getCell(3) != null ? row.getCell(3).getStringCellValue().trim() : null;

                // Validate name
                if (name == null || name.isEmpty()) {
                    errors.add(String.format("Row %d: Artist name cannot be null or empty.", i + 1));
                    continue;
                }

                // Check trùng
                Optional<Artist> existingArtist = artistService.findByName(name);
                if (existingArtist.isPresent()) {
                    errors.add(String.format("Row %d: Artist with name '%s' already exists.", i + 1, name));
                    continue;
                }

                // Map DTO
                ArtistDTO dto = new ArtistDTO();
                dto.setName(name);
                dto.setCountry(country);
                dto.setDebutYear(debutYear);
                dto.setDescription(description);

                artistService.create(dto);
            }

            if (!errors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Import failed with the following errors: " + String.join("; ", errors));
            }
            return ResponseEntity.ok("Artists imported successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error importing artists: " + e.getMessage());
        }
    }
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=artists.xlsx");

        List<ArtistDTO> artists = artistService.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Artists");

        // Header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");
        header.createCell(1).setCellValue("Country");
        header.createCell(2).setCellValue("Debut Year");
        header.createCell(3).setCellValue("Description");

        // Data
        for (int i = 0; i < artists.size(); i++) {
            ArtistDTO artist = artists.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(artist.getName() != null ? artist.getName() : "");
            row.createCell(1).setCellValue(artist.getCountry() != null ? artist.getCountry() : "");
            row.createCell(2).setCellValue(artist.getDebutYear() != null ? artist.getDebutYear() : 0);
            row.createCell(3).setCellValue(artist.getDescription() != null ? artist.getDescription() : "");
        }

        // Auto resize column
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }
    
        @GetMapping("/count")
        public ResponseEntity<Long> getArtistCount(@RequestParam(required = false) String search) {
            long count = (search != null && !search.isEmpty()) ?
                    artistService.countBySearchCriteria(search) : artistService.countArtists();
            return ResponseEntity.ok(count);
        }
    }
