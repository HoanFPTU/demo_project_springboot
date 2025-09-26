package com.example.starter_project.systems.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "content_moderations")
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentModeration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Content name cannot be blank")
    @Size(max = 50, message = "Content name must not exceed 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String contentName;

    @NotBlank(message = "Status cannot be blank")
    private String status; // pending, approved, rejected

    private String reason; // lý do nếu rejected
}
