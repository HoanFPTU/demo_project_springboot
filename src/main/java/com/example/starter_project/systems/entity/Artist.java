package com.example.starter_project.systems.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "artists")
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Artist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Size(max = 50)
    @Column(unique = true)
    private String name;
    private String country;
    private Integer debutYear;
    private String description;
    // mappedBy trỏ về field "artists" trong Song
    @ManyToMany(mappedBy = "artists")
    private Set<Song> songs = new HashSet<>();

}
