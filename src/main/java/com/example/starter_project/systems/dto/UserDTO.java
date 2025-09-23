package com.example.starter_project.systems.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String roleName;
}
