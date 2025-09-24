package com.example.starter_project.systems.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
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
    @Size(max = 100)
    @Column(unique = true)
    private String name;

    @Size(max = 300)
    private String description;

    private Boolean isPublic = true;

    private Integer songLimit = 500;

    private LocalDate dateUpdate;

    // Lưu list bài hát trực tiếp vào bảng playlist_songs
    @ElementCollection
    @CollectionTable(
            name = "playlist_songs",
            joinColumns = @JoinColumn(name = "playlist_id")
    )
    @Column(name = "song_name")
    private List<String> songs;
}
