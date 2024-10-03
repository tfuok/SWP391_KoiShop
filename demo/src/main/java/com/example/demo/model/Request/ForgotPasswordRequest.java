package com.example.demo.model.Request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @Email(message = "Email not valid")
    String email;
}
