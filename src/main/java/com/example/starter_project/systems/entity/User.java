package com.example.starter_project.systems.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private  String password;
    private String email;
    private String phone;
    private String address;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;



}
