package com.example.starter_project.systems.mapper;

import com.example.starter_project.systems.dto.ContentModerationDTO;
import com.example.starter_project.systems.entity.ContentModeration;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContentModerationMapper {
    ContentModerationDTO toDTO(ContentModeration entity);
    ContentModeration toEntity(ContentModerationDTO dto);
    List<ContentModerationDTO> toDTOList(List<ContentModeration> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ContentModerationDTO dto, @MappingTarget ContentModeration entity);
}
