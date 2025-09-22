package com.example.starter_project.systems.mapper;

import com.example.starter_project.systems.dto.RoleDTO;
import com.example.starter_project.systems.entity.Role;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;


@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleDTO toDTO(Role entity);
    Role toEntity(RoleDTO dto);
    List<RoleDTO> toDTOList(List<Role> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(RoleDTO dto, @MappingTarget Role entity);
}
