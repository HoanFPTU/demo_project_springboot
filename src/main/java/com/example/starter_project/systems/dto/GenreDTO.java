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
public class GenreDTO {
    private Long id;
    @NotNull(message = "Genre name cannot be null")
    @Size(max = 50, message = "Genre name must not exceed 50 characters")
    @NotBlank(message = "Genre name cannot be blank")
    private String name;
}
