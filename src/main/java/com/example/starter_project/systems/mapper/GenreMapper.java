package com.example.starter_project.systems.mapper;

import com.example.starter_project.systems.dto.GenreDTO;
import com.example.starter_project.systems.entity.Genre;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;


@Mapper(componentModel = "spring")
public interface GenreMapper {
    GenreDTO toDTO(Genre entity);
    Genre toEntity(GenreDTO dto);
    List<GenreDTO> toDTOList(List<Genre> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(GenreDTO dto, @MappingTarget Genre entity);
}
