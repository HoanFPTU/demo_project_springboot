package com.example.starter_project.systems.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDTO {
    private Long id;
    @NotNull(message = "Report name cannot be null")
    @Size(max = 50, message = "Report name must not exceed 50 characters")
    @NotBlank(message = "Report name cannot be blank")
    private String name;
    @Size(max = 200, message = "Report description must not exceed 200 characters")
    private String description;
}
