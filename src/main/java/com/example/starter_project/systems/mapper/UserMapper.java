package com.example.starter_project.systems.mapper;

import com.example.starter_project.systems.dto.UserDTO;
import com.example.starter_project.systems.entity.User;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // Entity -> DTO
    @Mapping(source = "role.id", target = "roleId")
    UserDTO toDTO(User entity);

    // DTO -> Entity (role set thủ công trong Service)
    @Mapping(target = "role", ignore = true)
    User toEntity(UserDTO dto);

    // List<Entity> -> List<DTO>
    List<UserDTO> toDTOList(List<User> entities);

    // Update entity từ DTO (chỉ update field khác null)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "role", ignore = true) // role xử lý trong Service
    void updateEntityFromDto(UserDTO dto, @MappingTarget User entity);

}
