package com.example.starter_project.systems.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaylistDTO {
    private Long id;

    @NotNull(message = "Playlist name cannot be null")
    @Size(max = 100, message = "Playlist name must not exceed 100 characters")
    @NotBlank(message = "Playlist name cannot be blank")
    private String name;

    @Size(max = 300, message = "Description must not exceed 300 characters")
    private String description;

    private Boolean isPublic;

    private Integer songLimit;

    private LocalDate dateUpdate;

    private List<String> songs;
}
