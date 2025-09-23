package com.example.starter_project.systems.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "songs")
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(unique = true)
    private String name;
//    private String artist;
//    private String album;
    private Integer year;
//    private String genre;

}
