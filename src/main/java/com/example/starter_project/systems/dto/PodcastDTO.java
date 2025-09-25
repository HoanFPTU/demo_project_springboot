package com.example.starter_project.systems.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PodcastDTO {
    private Long id;
    @NotNull(message = "Podcast name cannot be null")
    @Size(max = 50, message = "Podcast name must not exceed 50 characters")
    @NotBlank(message = "Podcast name cannot be blank")
    private String name;
}
