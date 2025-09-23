package com.example.starter_project.systems.mapper;

import com.example.starter_project.systems.dto.PlaylistDTO;
import com.example.starter_project.systems.entity.Playlist;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;


@Mapper(componentModel = "spring")
public interface PlaylistMapper {
    PlaylistDTO toDTO(Playlist entity);
    Playlist toEntity(PlaylistDTO dto);
    List<PlaylistDTO> toDTOList(List<Playlist> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(PlaylistDTO dto, @MappingTarget Playlist entity);
}
