package com.example.starter_project.systems.mapper;

import com.example.starter_project.systems.dto.ReportDTO;
import com.example.starter_project.systems.entity.Report;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;


@Mapper(componentModel = "spring")
public interface ReportMapper {
    ReportDTO toDTO(Report entity);
    Report toEntity(ReportDTO dto);
    List<ReportDTO> toDTOList(List<Report> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ReportDTO dto, @MappingTarget Report entity);
}

