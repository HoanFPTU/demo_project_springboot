package com.example.starter_project.systems.controller;

import com.example.starter_project.systems.dto.PlaylistDTO;
import com.example.starter_project.systems.entity.Playlist;
import com.example.starter_project.systems.service.PlaylistService;
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
@RequestMapping("/api/playlists")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid PlaylistDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Playlist name cannot be null or empty.");
        }
        Optional<Playlist> existingPlaylist = playlistService.findByName(dto.getName());
        if (existingPlaylist.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("Error: Playlist with name '%s' already exists.", dto.getName()));
        }
        try {
            PlaylistDTO createdPlaylist = playlistService.create(dto);
            return ResponseEntity.ok(createdPlaylist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to create playlist due to internal error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<PlaylistDTO>> getAll(
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
            return ResponseEntity.ok(playlistService.searchPlaylists(search, page, size, sortBy, sortDirection));
        }
        return ResponseEntity.ok(playlistService.findAllPaged(page, size, sortBy, sortDirection));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid PlaylistDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Playlist name cannot be null or empty.");
        }
        Optional<Playlist> playlistById = playlistService.findById(id);
        if (!playlistById.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("Error: Playlist with ID '%d' not found.", id));
        }
        Optional<Playlist> existingPlaylist = playlistService.findByName(dto.getName());
        if (existingPlaylist.isPresent() && !existingPlaylist.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("Error: Playlist with name '%s' already exists.", dto.getName()));
        }
        try {
            PlaylistDTO updatedPlaylist = playlistService.update(id, dto);
            return ResponseEntity.ok(updatedPlaylist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to update playlist due to internal error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
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
                    errors.add(String.format("Row %d: Playlist name cannot be null or empty.", i + 1));
                    continue;
                }
                Optional<Playlist> existingPlaylist = playlistService.findByName(name);
                if (existingPlaylist.isPresent()) {
                    errors.add(String.format("Row %d: Playlist with name '%s' already exists.", i + 1, name));
                    continue;
                }
                PlaylistDTO dto = new PlaylistDTO();
                dto.setName(name);
                playlistService.create(dto);
            }
            if (!errors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Import failed with the following errors: " + String.join("; ", errors));
            }
            return ResponseEntity.ok("Playlists imported successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error importing playlists: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=playlists.xlsx");
        List<PlaylistDTO> playlists = playlistService.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Playlists");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");

        for (int i = 0; i < playlists.size(); i++) {
            PlaylistDTO playlist = playlists.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(playlist.getName());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getPlaylistCount(@RequestParam(required = false) String search) {
        long count = (search != null && !search.isEmpty()) ?
                playlistService.countBySearchCriteria(search) : playlistService.countPlaylists();
        return ResponseEntity.ok(count);
    }
}