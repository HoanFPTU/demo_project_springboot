package com.example.starter_project.systems.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    private String artistName;

    private List<String> songs;
//    private LocalDateTime createdAt;
    private LocalDate releaseDate;
}
