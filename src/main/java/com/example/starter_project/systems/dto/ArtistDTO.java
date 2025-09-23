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
public class ArtistDTO {
    private Long id;
    @NotNull(message = "Artist name cannot be null")
    @Size(max = 50, message = "Artist name must not exceed 50 characters")
    @NotBlank(message = "Artist name cannot be blank")
    private String name;
    private String country;
    private Integer debutYear;
    private String description;
}