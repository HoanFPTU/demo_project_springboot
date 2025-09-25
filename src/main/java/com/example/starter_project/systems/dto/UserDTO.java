package com.example.starter_project.systems.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NonNull;

@Data
public class UserDTO {
    private Long id;
    private String name;
    @Email(message = "Invalid email format")
    private String email;
    private String phone;
    @Size(max = 100)
    private String address;
    private Long roleId;
}
