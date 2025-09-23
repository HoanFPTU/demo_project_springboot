package com.example.starter_project.systems.repository;

import com.example.starter_project.systems.entity.Role;
import com.example.starter_project.systems.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    // Tìm user theo email
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);



    // Tìm user theo số điện thoại
    Optional<User> findByPhone(String phone);

    // Kiểm tra tồn tại user bằng email
    boolean existsByEmail(String email);

    // Kiểm tra tồn tại user bằng phone
    boolean existsByPhone(String phone);

    // Tìm user theo name (ignore case)
    Page<User> findByNameContainingIgnoreCase(String query, Pageable pageable);
}
