package com.example.demo.model;

import lombok.Data;

@Data
public class AccountResponse {
    long id;
    String code;
    String email;
    String phone;
    String token;
}
