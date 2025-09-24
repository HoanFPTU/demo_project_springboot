package com.example.starter_project.systems.mapper;

import com.example.starter_project.systems.dto.QuizDetailDTO;
import com.example.starter_project.systems.entity.QuizDetail;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuizDetailMapper {
    QuizDetailDTO toDTO(QuizDetail entity);
    QuizDetail toEntity(QuizDetailDTO dto);
    List<QuizDetailDTO> toDTOList(List<QuizDetail> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(QuizDetailDTO dto, @MappingTarget QuizDetail entity);
}
