package com.example.demo.model;

import com.example.demo.entity.Role;
import lombok.Data;

@Data
public class AccountResponse {
    long id;
    String email;
    String phone;
    Role role;
    String token;
}
