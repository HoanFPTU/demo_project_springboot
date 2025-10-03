package com.example.starter_project.systems.controller;

import com.example.starter_project.systems.dto.SimpleDTO;
import com.example.starter_project.systems.dto.SongDTO;
import com.example.starter_project.systems.entity.Artist;
import com.example.starter_project.systems.entity.Song;
import com.example.starter_project.systems.service.SongService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    @Autowired
    private SongService songService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid SongDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Song name cannot be null or empty.");
        }
        try {
            SongDTO createdSong = songService.create(dto);
            return ResponseEntity.ok(createdSong);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to create song due to internal error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<SongDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            @RequestParam(required = false) String search
    ) {
        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "asc";

        // ✅ Whitelist allowed sort fields for safety
        List<String> allowedSortFields = List.of("id", "name","releaseYear");
        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "name";
        }

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(songService.searchSongs(search, page, size, sortBy, sortDirection));
        }
        return ResponseEntity.ok(songService.findAllPaged(page, size, sortBy, sortDirection));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid SongDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Song name cannot be null or empty.");
        }
        Optional<Song> songById = songService.findById(id);
        if (!songById.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("Error: Song with ID '%d' not found.", id));
        }
        Optional<Song> existingSong = songService.findByName(dto.getName());
        if (existingSong.isPresent() && !existingSong.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("Error: Song with name '%s' already exists.", dto.getName()));
        }
        try {
            SongDTO updatedSong = songService.update(id, dto);
            return ResponseEntity.ok(updatedSong);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to update song due to internal error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> errors = new ArrayList<>();
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // bỏ row header
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Cột 0: name
                String name = row.getCell(0) != null ? formatter.formatCellValue(row.getCell(0)).trim() : null;

                // Cột 1: artistIds (ví dụ: "1,2,3")
                String artistStr = row.getCell(1) != null ? formatter.formatCellValue(row.getCell(1)).trim() : null;
                List<Long> artistIds = new ArrayList<>();
                if (artistStr != null && !artistStr.isEmpty()) {
                    try {
                        artistIds = Arrays.stream(artistStr.split(","))
                                .map(String::trim)
                                .map(Long::parseLong)
                                .collect(Collectors.toList());
                    } catch (NumberFormatException e) {
                        errors.add(String.format("Row %d: Invalid artist IDs format (must be numbers separated by commas).", i + 1));
                    }
                }

                // Cột 2: releaseYear
                Integer releaseYear = null;
                if (row.getCell(2) != null) {
                    if (row.getCell(2).getCellType() == CellType.NUMERIC) {
                        releaseYear = (int) row.getCell(2).getNumericCellValue();
                    } else {
                        errors.add(String.format("Row %d: ReleaseYear must be a number.", i + 1));
                    }
                }

                // Cột 3: genre
                String genre = row.getCell(3) != null ? formatter.formatCellValue(row.getCell(3)).trim() : null;

                // Validate name
                if (name == null || name.isEmpty()) {
                    errors.add(String.format("Row %d: Song name cannot be null or empty.", i + 1));
                    continue;
                }

                // Map DTO
                SongDTO dto = new SongDTO();
                dto.setName(name);
                dto.setArtistIds(artistIds);
                dto.setReleaseYear(releaseYear);
                dto.setGenre(genre);

                songService.create(dto);
            }

            if (!errors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Import failed with the following errors: " + String.join("; ", errors));
            }
            return ResponseEntity.ok("Songs imported successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error importing songs: " + e.getMessage());
        }
    }
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=songs.xlsx");

        List<SongDTO> songs = songService.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Songs");

        // Header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");
        header.createCell(1).setCellValue("Artist");
        header.createCell(2).setCellValue("Release Year");
        header.createCell(3).setCellValue("Genre");

        // Data rows
        for (int i = 0; i < songs.size(); i++) {
            SongDTO song = songs.get(i);
            Row row = sheet.createRow(i + 1);


            row.createCell(0).setCellValue(song.getName() != null ? song.getName() : "");
            row.createCell(1).setCellValue(
                    song.getArtists() != null && !song.getArtists().isEmpty()
                            ? song.getArtists().stream()
                            .map(dto -> String.valueOf(dto.getId())) // lấy ID ca sĩ dạng chuỗi
                            .collect(Collectors.joining(", "))
                            : ""
            );
            row.createCell(2).setCellValue(song.getReleaseYear() != null ? song.getReleaseYear() : 0);
            row.createCell(3).setCellValue(song.getGenre() != null ? song.getGenre() : "");
        }

        // Auto size columns
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getSongCount(@RequestParam(required = false) String search) {
        long count = (search != null && !search.isEmpty()) ?
                songService.countBySearchCriteria(search) : songService.countSongs();
        return ResponseEntity.ok(count);
    }
}
