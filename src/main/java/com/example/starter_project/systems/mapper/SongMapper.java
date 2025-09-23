package com.example.starter_project.systems.mapper;

import com.example.starter_project.systems.dto.SongDTO;
import com.example.starter_project.systems.entity.Song;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;


@Mapper(componentModel = "spring")
public interface SongMapper {
    SongDTO toDTO(Song entity);
    Song toEntity(SongDTO dto);
    List<SongDTO> toDTOList(List<Song> entities);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SongDTO dto, @MappingTarget Song entity);
}
