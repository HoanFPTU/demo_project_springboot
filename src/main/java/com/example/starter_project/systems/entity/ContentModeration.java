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
@Table(name = "content_moderations", uniqueConstraints = {
        @UniqueConstraint(columnNames = "contentName")
})
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentModeration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Content name cannot be blank")
    @Size(max = 50, message = "Content name must not exceed 50 characters")
    @Column(name = "contentName", unique = true, nullable = false, length = 50)
    private String contentName;

    @NotBlank(message = "Status cannot be blank")
    @Column(nullable = false, length = 20)
    private String status; // pending, approved, rejected

    @Size(max = 255)
    private String reason; // lý do nếu rejected
}
