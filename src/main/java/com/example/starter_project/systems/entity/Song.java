package com.example.starter_project.systems.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
//@ManyToOne
//@JoinColumn(name = "album_id")  // FK trong bảng songs
//private Album album;

    private Integer releaseYear;
    private String genre;
    @ManyToMany
    private Set<Artist> artists;
}
