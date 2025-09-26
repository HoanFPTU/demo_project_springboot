package com.example.starter_project.systems.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentModerationDTO {
    private Long id;

    @NotBlank(message = "Content name cannot be blank")
    @Size(max = 50, message = "Content name must not exceed 50 characters")
    private String contentName;

    @NotBlank(message = "Status cannot be blank")
    private String status;

    private String reason;
}
