package com.example.starter_project.systems.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "albums")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 50)
    @Column(unique = true)
    private String name;

    private String artistName;

    // Lưu list bài hát trực tiếp vào bảng album_songs
    @ElementCollection
    @CollectionTable(
            name = "album_songs",
            joinColumns = @JoinColumn(name = "album_id")
    )
    @Column(name = "song_name")
    private List<String> songs;

    // ngày tạo album
//    @Column(updatable = false)
//    @CreationTimestamp
//    private LocalDateTime createdAt;
    private LocalDate releaseDate;
}
