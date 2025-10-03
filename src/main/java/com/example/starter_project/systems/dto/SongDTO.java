package com.example.starter_project.systems.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongDTO {
    private Long id;
    @NotNull(message = "Song name cannot be null")
    @Size(max = 50, message = "Song name must not exceed 50 characters")
    @NotBlank(message = "Song name cannot be blank")
    private String name;


    private Integer releaseYear;
    private String genre;
    // input
    private List<Long> artistIds;

    // output
    private List<SimpleDTO> artists; // id + name
}