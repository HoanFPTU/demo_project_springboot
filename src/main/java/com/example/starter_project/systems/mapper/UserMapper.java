package com.example.starter_project.systems.mapper;

import com.example.starter_project.systems.dto.UserDTO;
import com.example.starter_project.systems.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses =  {RoleMapper.class})
public interface UserMapper {
    // Entity -> DTO
    @Mapping(source = "role.name", target = "roleName")
    UserDTO toDTO(User entity);

    // DTO -> Entity
    @Mapping(source = "roleName", target = "role.name")
    User toEntity(UserDTO dto);

    // List<Entity> -> List<DTO>
    List<UserDTO> toDTOList(List<User> entities);

    // Cập nhật Entity từ DTO (dùng cho update)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UserDTO dto, @MappingTarget User entity);

}
