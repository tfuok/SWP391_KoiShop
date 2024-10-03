package com.example.demo.model.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Password must not be blank!")
    @Size(min = 6, message = "Password must at least 6 character!")
    String password;
}
