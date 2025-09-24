package com.example.starter_project.systems.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "quiz_details")
@Builder
public class QuizDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(unique = true)
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull
    @Min(1)
    private Integer totalQuestions;

    @NotNull
    @Min(30)
    private Integer durationSeconds;

    @NotBlank
    @Size(max = 20)
    private String level;

    private Long createdBy;
    private LocalDateTime createdAt;
    private Boolean active;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;
}

