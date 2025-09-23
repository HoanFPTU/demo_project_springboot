package com.example.starter_project.systems.controller;

import com.example.starter_project.systems.dto.UserDTO;
import com.example.starter_project.systems.dto.UserDTO;
import com.example.starter_project.systems.entity.User;
import com.example.starter_project.systems.entity.User;
import com.example.starter_project.systems.mapper.UserMapper;
import com.example.starter_project.systems.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid UserDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: User name cannot be null or empty.");
        }
        Optional<User> existingUser = userService.findByEmail(dto.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("Error: User with name '%s' already exists.", dto.getName()));
        }
        try {
            UserDTO createdUser = userService.create(dto);
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to create user due to internal error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAll(
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
            return ResponseEntity.ok(userService.search(search, page, size, sortBy, sortDirection));
        }
        return ResponseEntity.ok(userService.findAllPaged(page, size, sortBy, sortDirection));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody @Valid UserDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: User name cannot be null or empty.");
        }
        Optional<User> userById = userService.findById(id);
        if (!userById.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("Error: User with ID '%d' not found.", id));
        }
        Optional<User> existingUser = userService.findByEmail(dto.getEmail());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(String.format("Error: User with name '%s' already exists.", dto.getName()));
        }
        try {
            UserDTO updatedUser = userService.update(id, dto);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: Failed to update user due to internal error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
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
                    errors.add(String.format("Row %d: User name cannot be null or empty.", i + 1));
                    continue;
                }
                Optional<User> existingUser = userService.findByName(name);
                if (existingUser.isPresent()) {
                    errors.add(String.format("Row %d: User with name '%s' already exists.", i + 1, name));
                    continue;
                }
                UserDTO dto = new UserDTO();
                dto.setName(name);
                userService.create(dto);
            }
            if (!errors.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Import failed with the following errors: " + String.join("; ", errors));
            }
            return ResponseEntity.ok("Users imported successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error importing users: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");
        List<UserDTO> users = userService.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Name");

        for (int i = 0; i < users.size(); i++) {
            UserDTO user = users.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(user.getName());
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }


}
