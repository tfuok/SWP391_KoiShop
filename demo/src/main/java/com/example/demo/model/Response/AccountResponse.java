package com.example.demo.model.Response;

import com.example.demo.entity.Role;
import lombok.Data;

@Data
public class AccountResponse {
    long id;
    String email;
    String phone;
    String username;
    Role role;
    String address;
    String token;
}
