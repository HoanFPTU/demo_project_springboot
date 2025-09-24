package com.example.starter_project.systems.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDetailDTO {
    private Long id;
    private String name;
    private String description;
    private Integer totalQuestions;
    private Integer durationSeconds;
    private String level;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Boolean active;
    private List<QuestionDTO> questions;
}
