package com.example.starter_project.systems.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "playlists")
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(unique = true)
    private String name;
//    @ManyToMany
//    @JoinTable(
//            name = "playlist_song",
//            joinColumns = @JoinColumn(name = "playlist_id"),
//            inverseJoinColumns = @JoinColumn(name = "song_id")
//    )
//    private List<Song> songs = new ArrayList<>();

}
