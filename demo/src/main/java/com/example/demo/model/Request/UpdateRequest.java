package com.example.demo.model.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateRequest {
    @Email(message = "Email not valid")
    String email;

    @NotBlank(message = "Username must not be blank!")
    String username;

    @Pattern(regexp = "(84|0[3|5|7|8|9])+(\\d{8})", message = "Invalid phone number!")
    String phone;

    String address;
}
