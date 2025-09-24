package com.example.starter_project.systems.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "quiz_answers")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @NotBlank
    private String content;

    private Boolean isCorrect;
}
