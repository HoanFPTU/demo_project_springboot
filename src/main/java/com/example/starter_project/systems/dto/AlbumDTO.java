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
public class AlbumDTO {
    private Long id;
    @NotNull(message = "Album name cannot be null")
    @Size(max = 50, message = "Album name must not exceed 50 characters")
    @NotBlank(message = "Album name cannot be blank")
    private String name;
}
