package com.example.starter_project.systems.service;

import com.example.starter_project.systems.dto.UserDTO;
import com.example.starter_project.systems.dto.UserDTO;
import com.example.starter_project.systems.entity.Role;
import com.example.starter_project.systems.entity.User;
import com.example.starter_project.systems.entity.User;
import com.example.starter_project.systems.mapper.UserMapper;
import com.example.starter_project.systems.repository.RoleRepository;
import com.example.starter_project.systems.repository.UserRepository;
import com.example.starter_project.systems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleRepository roleRepository;


    private String validateSortBy(String sortBy) {
        List<String> allowedSortFields = List.of("id", "name");
        return allowedSortFields.contains(sortBy) ? sortBy : "name";
    }
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }



    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<UserDTO> findAll() {
        List<User> users = userRepository.findAll();
        return userMapper.toDTOList(users);
    }
    public Page<UserDTO> findAllPaged(int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = userRepository.findAll(pageable);
        return users.map(userMapper::toDTO);
    }

    public Page<UserDTO> search(String query, int page, int size, String sortBy, String sortDirection) {
        sortBy = validateSortBy(sortBy);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = userRepository.findByNameContainingIgnoreCase(query, pageable);
        return users.map(userMapper::toDTO);
    }
    public UserDTO create(UserDTO dto) {
//        if (userRepository.existsByEmail(user.getEmail())) {
//            throw new RuntimeException("Email already exists");
//        }
//        if (userRepository.existsByPhone(user.getPhone())) {
//            throw new RuntimeException("Phone already exists");
//        }
//        return userRepository.save(user);
        User entity = userMapper.toEntity(dto);
        Long roleId = (dto.getRoleId() == null) ? 1L : dto.getRoleId();

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        entity.setRole(role);
        User saved = userRepository.save(entity);
        return userMapper.toDTO(saved);
    }

//    public User updateUser(Long id, User userDetails) {
//        return userRepository.findById(id).map(user -> {
//            user.setName(userDetails.getName());
//            user.setEmail(userDetails.getEmail());
//            user.setPhone(userDetails.getPhone());
//            user.setAddress(userDetails.getAddress());
//            user.setUser(userDetails.getUser());
//            return userRepository.save(user);
//        }).orElseThrow(() -> new RuntimeException("User not exists"));
//    }
public UserDTO update(Long id, UserDTO dto) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    userMapper.updateEntityFromDto(dto, user);
    User updated = userRepository.save(user);
    return userMapper.toDTO(updated);
}


//    public void deleteUser(Long id) {
//        if (!userRepository.existsById(id)) {
//            throw new RuntimeException("User not exists");
//        }
//        userRepository.deleteById(id);
//    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }



}

