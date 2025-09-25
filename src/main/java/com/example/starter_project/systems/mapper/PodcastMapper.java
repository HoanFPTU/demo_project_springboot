package com.example.starter_project.systems.mapper;

import com.example.starter_project.systems.dto.PodcastDTO;
import com.example.starter_project.systems.entity.Podcast;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
@Mapper(componentModel = "spring")
public interface PodcastMapper {
    PodcastDTO toDTO(Podcast entity);
    Podcast toEntity(PodcastDTO dto);
    List<PodcastDTO> toDTOList(List<Podcast> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(PodcastDTO dto, @MappingTarget Podcast entity);
}
