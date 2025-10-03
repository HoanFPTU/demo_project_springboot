package com.example.starter_project.systems.controller;

import com.example.starter_project.systems.dto.ReportDTO;
import com.example.starter_project.systems.entity.Report;
import com.example.starter_project.systems.service.ReportService;
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
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid ReportDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Report name cannot be null or empty.");
        }
        Optional<Report> existingReport = reportService.findByName(dto.getName());
        if (existingReport.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("Error: Report with name '%s' already exists.", dto.getName()));
        }
        try {
            ReportDTO createdReport = reportService.create(dto);
            return ResponseEntity.ok(createdReport);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to create report due to internal error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<ReportDTO>> getAll(
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
            return ResponseEntity.ok(reportService.searchReports(search, page, size, sortBy, sortDirection));
        }
        return ResponseEntity.ok(reportService.findAllPaged(page, size, sortBy, sortDirection));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid ReportDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: Report name cannot be null or empty.");
        }
        Optional<Report> reportById = reportService.findById(id);
        if (!reportById.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("Error: Report with ID '%d' not found.", id));
        }
        Optional<Report> existingReport = reportService.findByName(dto.getName());
        if (existingReport.isPresent() && !existingReport.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("Error: Report with name '%s' already exists.", dto.getName()));
        }
        try {
            ReportDTO updatedReport = reportService.update(id, dto);
            return ResponseEntity.ok(updatedReport);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to update report due to internal error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reportService.deleteReport(id);
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
                    errors.add(String.format("Row %d: Report name cannot be null or empty.", i + 1));
                    continue;
                }
                Optional<Report> existingReport = reportService.findByName(name);
                if (existingReport.isPresent()) {
                    errors.add(String.format("Row %d: Report with name '%s' already exists.", i + 1, name));
                    continue;
                }
                ReportDTO dto = new ReportDTO();
                dto.setName(name);
                dto.setDescription(row.getCell(1) != null ? row.getCell(1).getStringCellValue() : null);
                reportService.create(dto);
            }
            if (!errors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Import failed with the following errors: " + String.join("; ", errors));
            }
            return ResponseEntity.ok("Reports imported successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error importing reports: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=reports.xlsx");
        List<ReportDTO> reports = reportService.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reports");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");
        header.createCell(1).setCellValue("Description");

        for (int i = 0; i < reports.size(); i++) {
            ReportDTO report = reports.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(report.getName());
            row.createCell(1).setCellValue(report.getDescription());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getReportCount(@RequestParam(required = false) String search) {
        long count = (search != null && !search.isEmpty()) ?
                reportService.countBySearchCriteria(search) : reportService.countReports();
        return ResponseEntity.ok(count);
    }
}