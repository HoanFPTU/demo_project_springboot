package com.example.starter_project.systems.mapper;

import com.example.starter_project.systems.dto.AlbumDTO;
import com.example.starter_project.systems.entity.Album;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AlbumMapper {

    AlbumDTO toDTO(Album entity);

    Album toEntity(AlbumDTO dto);

    List<AlbumDTO> toDTOList(List<Album> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(AlbumDTO dto, @MappingTarget Album entity);
}
