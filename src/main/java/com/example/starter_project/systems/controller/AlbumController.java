package com.example.starter_project.systems.controller;

import com.example.starter_project.systems.dto.AlbumDTO;
import com.example.starter_project.systems.entity.Album;
import com.example.starter_project.systems.service.AlbumService;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid AlbumDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Album name cannot be null or empty.");
        }
        Optional<Album> existingAlbum = albumService.findByName(dto.getName());
        if (existingAlbum.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("Error: Album with name '%s' already exists.", dto.getName()));
        }
        try {
            AlbumDTO createdAlbum = albumService.create(dto);
            return ResponseEntity.ok(createdAlbum);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to create album due to internal error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<AlbumDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            @RequestParam(required = false) String search
    ) {
        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "asc";

        List<String> allowedSortFields = List.of("id", "name");
        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "name";
        }

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(albumService.searchAlbums(search, page, size, sortBy, sortDirection));
        }
        return ResponseEntity.ok(albumService.findAllPaged(page, size, sortBy, sortDirection));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid AlbumDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Album name cannot be null or empty.");
        }
        Optional<Album> albumById = albumService.findById(id);
        if (!albumById.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("Error: Album with ID '%d' not found.", id));
        }
        Optional<Album> existingAlbum = albumService.findByName(dto.getName());
        if (existingAlbum.isPresent() && !existingAlbum.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("Error: Album with name '%s' already exists.", dto.getName()));
        }
        try {
            AlbumDTO updatedAlbum = albumService.update(id, dto);
            return ResponseEntity.ok(updatedAlbum);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to update album due to internal error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        albumService.deleteAlbum(id);
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
                String artistName = row.getCell(1) != null ? row.getCell(1).getStringCellValue() : null;
                String songsStr = row.getCell(2) != null ? row.getCell(2).getStringCellValue() : null;
                String releaseDateStr = row.getCell(3) != null ? row.getCell(3).getStringCellValue() : null;

                List<String> songs = new ArrayList<>();
                if (songsStr != null && !songsStr.trim().isEmpty()) {
                    for (String s : songsStr.split(",")) {
                        songs.add(s.trim());
                    }
                }

                if (name == null || name.trim().isEmpty()) {
                    errors.add(String.format("Row %d: Album name cannot be null or empty.", i + 1));
                    continue;
                }
                Optional<Album> existingAlbum = albumService.findByName(name);
                if (existingAlbum.isPresent()) {
                    errors.add(String.format("Row %d: Album with name '%s' already exists.", i + 1, name));
                    continue;
                }

                LocalDate releaseDate = null;
                try {
                    if (releaseDateStr != null && !releaseDateStr.trim().isEmpty()) {
                        releaseDate = LocalDate.parse(releaseDateStr); // yyyy-MM-dd
                    }
                } catch (Exception e) {
                    errors.add(String.format("Row %d: Invalid release date format, expected yyyy-MM-dd.", i + 1));
                }

                AlbumDTO dto = AlbumDTO.builder()
                        .name(name)
                        .artistName(artistName)
                        .songs(songs)
                        .releaseDate(releaseDate)
                        .build();
                albumService.create(dto);
            }

            if (!errors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Import failed with errors: " + String.join("; ", errors));
            }
            return ResponseEntity.ok("Albums imported successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error importing albums: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=albums.xlsx");
        List<AlbumDTO> albums = albumService.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Albums");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");
        header.createCell(1).setCellValue("Artist");
        header.createCell(2).setCellValue("Songs");
        header.createCell(3).setCellValue("ReleaseDate (yyyy-MM-dd)");

        for (int i = 0; i < albums.size(); i++) {
            AlbumDTO album = albums.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(album.getName());
            row.createCell(1).setCellValue(album.getArtistName() != null ? album.getArtistName() : "");
            row.createCell(2).setCellValue(album.getSongs() != null ? String.join(", ", album.getSongs()) : "");
            row.createCell(3).setCellValue(album.getReleaseDate() != null ? album.getReleaseDate().toString() : "");
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getAlbumCount(@RequestParam(required = false) String search) {
        long count = (search != null && !search.isEmpty()) ?
                albumService.countBySearchCriteria(search) : albumService.countAlbums();
        return ResponseEntity.ok(count);
    }
}
