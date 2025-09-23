package com.example.starter_project.systems.mapper;

import com.example.starter_project.systems.dto.ArtistDTO;
import com.example.starter_project.systems.entity.Artist;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;


@Mapper(componentModel = "spring")
public interface ArtistMapper {
    ArtistDTO toDTO(Artist entity);
    Artist toEntity(ArtistDTO dto);
    List<ArtistDTO> toDTOList(List<Artist> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ArtistDTO dto, @MappingTarget Artist entity);
}
