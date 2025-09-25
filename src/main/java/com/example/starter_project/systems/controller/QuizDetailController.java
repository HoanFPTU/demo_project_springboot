package com.example.starter_project.systems.controller;

import com.example.starter_project.systems.dto.QuizDetailDTO;
import com.example.starter_project.systems.service.QuizDetailService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/quizDetails")
public class QuizDetailController {

    @Autowired
    private QuizDetailService quizDetailService;

    // ===== CREATE =====
    @PostMapping
    public ResponseEntity<?> create(@RequestBody QuizDetailDTO dto) {
        try {
            QuizDetailDTO created = quizDetailService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ===== DELETE =====
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        quizDetailService.deleteQuizDetail(id);
        return ResponseEntity.noContent().build();
    }

    // ===== GET BY ID =====
    @GetMapping("/{id}")
    public ResponseEntity<QuizDetailDTO> getById(@PathVariable Long id){
        QuizDetailDTO quiz = quizDetailService.findByIdDTO(id);
        if(quiz == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(quiz);
    }

    // ===== GET ALL WITH PAGING & SEARCH =====
    @GetMapping
    public ResponseEntity<Page<QuizDetailDTO>> getAll(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(defaultValue = "name,asc") String sort,
                                                      @RequestParam(required = false) String search) {
        String[] arr = sort.split(",");
        String sortBy = arr[0];
        String sortDir = arr.length > 1 ? arr[1] : "asc";

        if(search != null && !search.isEmpty()) {
            return ResponseEntity.ok(quizDetailService.searchQuizDetails(search, page, size, sortBy, sortDir));
        }
        return ResponseEntity.ok(quizDetailService.findAllPaged(page, size, sortBy, sortDir));
    }

    // ===== COUNT =====
    @GetMapping("/count")
    public ResponseEntity<Long> count(@RequestParam(required = false) String search){
        long count = (search != null && !search.isEmpty()) ?
                quizDetailService.countBySearchCriteria(search) : quizDetailService.countQuizDetails();
        return ResponseEntity.ok(count);
    }

    // ===== EXPORT EXCEL =====
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=quizDetails.xlsx");

        List<QuizDetailDTO> quizDetails = quizDetailService.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("QuizDetails");

            // Header
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Description");
            header.createCell(2).setCellValue("DurationSeconds");
            header.createCell(3).setCellValue("Level");
            header.createCell(4).setCellValue("TotalQuestions");
            header.createCell(5).setCellValue("Active");

            // Data
            for (int i = 0; i < quizDetails.size(); i++) {
                QuizDetailDTO dto = quizDetails.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(dto.getName() != null ? dto.getName() : "");
                row.createCell(1).setCellValue(dto.getDescription() != null ? dto.getDescription() : "");
                row.createCell(2).setCellValue(dto.getDurationSeconds() != null ? dto.getDurationSeconds() : 0);
                row.createCell(3).setCellValue(dto.getLevel() != null ? dto.getLevel() : "");
                row.createCell(4).setCellValue(dto.getTotalQuestions() != null ? dto.getTotalQuestions() : 0);
                row.createCell(5).setCellValue(dto.getActive() != null ? dto.getActive() : false);
            }

            workbook.write(response.getOutputStream());
        }
    }

    // ===== IMPORT EXCEL =====
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded");
        }

        List<String> errors = new ArrayList<>();
        int successCount = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Bắt đầu từ row 1 vì row 0 là header
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String name = getCellValueAsString(row.getCell(0));
                String description = getCellValueAsString(row.getCell(1));
                Integer duration = getCellValueAsInteger(row.getCell(2));
                String level = getCellValueAsString(row.getCell(3));
                Integer totalQuestions = getCellValueAsInteger(row.getCell(4));
                Boolean active = getCellValueAsBoolean(row.getCell(5));

                // Validate
                if (name == null || name.trim().isEmpty()) {
                    errors.add(String.format("Row %d: Name cannot be null or empty.", i + 1));
                    continue;
                }

                boolean exists = quizDetailService.findAll()
                        .stream()
                        .anyMatch(q -> q.getName().equalsIgnoreCase(name));
                if (exists) {
                    errors.add(String.format("Row %d: QuizDetail with name '%s' already exists.", i + 1, name));
                    continue;
                }

                // Create DTO và set default cho field bắt buộc
                QuizDetailDTO dto = new QuizDetailDTO();
                dto.setName(name);
                dto.setDescription(description);
                dto.setDurationSeconds(duration);
                dto.setLevel(level);
                dto.setTotalQuestions(totalQuestions != null ? totalQuestions : 0);
                dto.setActive(active != null ? active : true);

                quizDetailService.create(dto);
                successCount++;
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error importing quizDetails: " + e.getMessage());
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Imported " + successCount + " rows successfully. Errors: " + String.join("; ", errors));
        }

        return ResponseEntity.ok("Imported " + successCount + " QuizDetails successfully.");
    }

    // ===== HELPER METHODS =====
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return null;
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Boolean getCellValueAsBoolean(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            return Boolean.parseBoolean(cell.getStringCellValue());
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue() != 0;
        }
        return null;
    }
}
