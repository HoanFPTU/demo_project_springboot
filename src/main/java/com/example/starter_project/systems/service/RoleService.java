package com.example.starter_project.systems.service;

import com.example.starter_project.systems.dto.RoleDTO;
import com.example.starter_project.systems.entity.Role;
import com.example.starter_project.systems.mapper.RoleMapper;
import com.example.starter_project.systems.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMapper roleMapper;

    private String validateSortBy(String sortBy) {
        List<String> allowedSortFields = List.of("id", "name");
        return allowedSortFields.contains(sortBy) ? sortBy : "name";
    }

    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    public List<RoleDTO> findAll() {
        List<Role> roles = roleRepository.findAll();
        return roleMapper.toDTOList(roles);
    }

    public Page<RoleDTO> findAllPaged(int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Role> roles = roleRepository.findAll(pageable);
        return roles.map(roleMapper::toDTO);
    }

    public Page<RoleDTO> searchRoles(String query, int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Role> roles = roleRepository.findByNameContainingIgnoreCase(query, pageable);
        return roles.map(roleMapper::toDTO);
    }

    public RoleDTO create(RoleDTO dto) {
        Role entity = roleMapper.toEntity(dto);
        Role saved = roleRepository.save(entity);
        return roleMapper.toDTO(saved);
    }

    public RoleDTO update(Long id, RoleDTO dto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        roleMapper.updateEntityFromDto(dto, role);
        Role updated = roleRepository.save(role);
        return roleMapper.toDTO(updated);
    }

    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }

    public long countRoles() {
        return roleRepository.count();
    }

    public long countBySearchCriteria(String search) {
        return roleRepository.countByNameContainingIgnoreCase(search);
    }
}