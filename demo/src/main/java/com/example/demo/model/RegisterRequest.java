package com.example.demo.model;

import com.example.demo.entity.Role;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class RegisterRequest {
    @NotBlank(message = "Code must not be blank")
    @Pattern(regexp = "KH\\d{6}", message = "Invalid code!")
    @Column(unique = true)
    String code;

    @Email(message = "Email not valid")
    String email;

    @Pattern(regexp = "(84|0[3|5|7|8|9])+(\\d{8})", message = "Invalid phone number!")
    String phone;

    @NotBlank(message = "Password must not be blank!")
    @Size(min = 6, message = "Password must at least 6 character!")
    String password;

    Role role;
}
