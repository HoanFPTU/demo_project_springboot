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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

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

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BLANK:
            default:
                return "";
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> errors = new ArrayList<>();

            // Bỏ header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // Cột 1: Name
                    String name = row.getCell(1) != null ? row.getCell(1).getStringCellValue().trim() : null;

                    // Cột 2: Description
                    String description = row.getCell(2) != null ? row.getCell(2).getStringCellValue().trim() : null;

                    // Cột 3: Public/Private
                    String publicValue = row.getCell(3) != null ? row.getCell(3).getStringCellValue().trim() : "Private";
                    Boolean isPublic = publicValue.equalsIgnoreCase("Public");

                    // Cột 4: Song limit
                    Integer songLimit = null;
                    if (row.getCell(4) != null) {
                        if (row.getCell(4).getCellType() == CellType.NUMERIC) {
                            songLimit = (int) row.getCell(4).getNumericCellValue();
                        } else {
                            errors.add(String.format("Row %d: Invalid song limit '%s'.", i + 1, row.getCell(4).toString()));
                            continue;
                        }
                    }

                    // Cột 5: Date Update
                    LocalDate dateUpdate = null;
                    if (row.getCell(5) != null) {
                        Cell cell = row.getCell(5);
                        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                            dateUpdate = cell.getLocalDateTimeCellValue().toLocalDate();
                        } else if (cell.getCellType() == CellType.STRING) {
                            try {
                                dateUpdate = LocalDate.parse(cell.getStringCellValue().trim());
                            } catch (Exception ex) {
                                errors.add(String.format("Row %d: Invalid date '%s'.", i + 1, cell.getStringCellValue()));
                                continue;
                            }
                        } else {
                            errors.add(String.format("Row %d: Invalid date '%s'.", i + 1, cell.toString()));
                            continue;
                        }
                    }

                    // Validate name
                    if (name == null || name.isEmpty()) {
                        errors.add(String.format("Row %d: Playlist name cannot be empty.", i + 1));
                        continue;
                    }

                    // Check duplicate
                    Optional<Playlist> existingPlaylist = playlistService.findByName(name);
                    if (existingPlaylist.isPresent()) {
                        errors.add(String.format("Row %d: Playlist with name '%s' already exists.", i + 1, name));
                        continue;
                    }

// Cột 7: Songs (danh sách ngăn cách bởi dấu phẩy)
                    List<String> songs = new ArrayList<>();
                    // ✅ đúng
                    if (row.getCell(6) != null) {
                        String songsStr = row.getCell(6).toString().trim();
                        if (!songsStr.isEmpty()) {
                            songs = Arrays.stream(songsStr.split(","))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .toList();
                        }
                    }


// Tạo DTO
                    PlaylistDTO dto = new PlaylistDTO();
                    dto.setName(name);
                    dto.setDescription(description);
                    dto.setIsPublic(isPublic);
                    dto.setSongLimit(songLimit);
                    if (dateUpdate != null) {
                        dto.setDateUpdate(dateUpdate);
                    }
                    dto.setSongs(songs); // ✅ set list bài hát

                    playlistService.create(dto);


                } catch (Exception ex) {
                    errors.add(String.format("Row %d: Unexpected error: %s", i + 1, ex.getMessage()));
                }
            }

            if (!errors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Import finished with some errors: " + String.join("; ", errors));
            }
            return ResponseEntity.ok("Playlists imported successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error importing playlists: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        List<Playlist> playlists = playlistService.findAllEntities();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Playlists");

        // Header
        Row header = sheet.createRow(0);
        String[] headers = {"ID", "Name", "Description", "Public", "Song Limit", "Date Update", "Songs"};
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }

        // Data
        int rowIdx = 1;
        for (Playlist p : playlists) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(p.getId());
            row.createCell(1).setCellValue(p.getName());
            row.createCell(2).setCellValue(p.getDescription() != null ? p.getDescription() : "");
            row.createCell(3).setCellValue(p.getIsPublic() != null && p.getIsPublic() ? "Public" : "Private");
            row.createCell(4).setCellValue(p.getSongLimit() != null ? p.getSongLimit() : 0);
            row.createCell(5).setCellValue(p.getDateUpdate() != null ? p.getDateUpdate().toString() : "");
            row.createCell(6).setCellValue(p.getSongs() != null ? String.join(", ", p.getSongs()) : "");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=playlists.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(out.toByteArray());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getPlaylistCount(@RequestParam(required = false) String search) {
        long count = (search != null && !search.isEmpty()) ?
                playlistService.countBySearchCriteria(search) : playlistService.countPlaylists();
        return ResponseEntity.ok(count);
    }
}
