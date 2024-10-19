package com.example.demo.model.Response;


import com.example.demo.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginGoogleResponse {
    long id;
    String email;
    String phone;
    String username;
    Role role;
    String address;
    String token;
}
