package com.example.starter_project.systems.mapper;

import com.example.starter_project.systems.dto.ArtistDTO;
import com.example.starter_project.systems.dto.SimpleDTO;
import com.example.starter_project.systems.dto.SongDTO;
import com.example.starter_project.systems.entity.Artist;
import com.example.starter_project.systems.entity.Song;
import com.example.starter_project.systems.repository.ArtistRepository;
import org.mapstruct.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
@Mapper(componentModel = "spring")
public interface SongMapper {
    // Entity -> DTO
    @Mapping(target = "artists", source = "artists")
    SongDTO toDTO(Song entity);

    default List<SimpleDTO> map(Set<Artist> artists) {
        if (artists == null) return null;
        return artists.stream()
                .map(a -> new SimpleDTO(a.getId(), a.getName()))
                .toList();
    }

    // DTO -> Entity
    @Mapping(target = "artists", ignore = true)
    Song toEntity(SongDTO dto);

    // List<Entity> -> List<DTO>
    List<SongDTO> toDTOList(List<Song> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SongDTO dto, @MappingTarget Song entity);

    // --- custom mapping methods ---

    // --- custom mapping ---
    @Named("toSimpleDTOList")
    default List<SimpleDTO> toSimpleDTOList(Set<Artist> artists) {
        if (artists == null) return null;
        return artists.stream()
                .map(a -> new SimpleDTO(a.getId(), a.getName()))
                .toList();
    }

    default Set<Artist> fromIds(List<Long> artistIds) {
        if (artistIds == null) return null;
        return artistIds.stream()
                .map(id -> { Artist a = new Artist(); a.setId(id); return a; })
                .collect(Collectors.toSet());
    }
}