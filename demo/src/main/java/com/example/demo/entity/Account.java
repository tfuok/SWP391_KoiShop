package com.example.demo.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class Account {
    long id;
    @NotBlank(message = "Code must not be blank")
    @Pattern(regexp = "KH\\d{6}", message = "Invalid code!")
    String code;
    @Email(message = "Email not valid")
    String email;
    @Pattern(regexp = "(84|0[3|5|7|8|9])+([0-9]{8})\\b", message = "Invalid phone number!")
    String phone;
    @NotBlank(message = "Password must not be blank!")
    @Size(min = 6, message = "Password must at least 6 character!")
    String password;
    Date signUpDate;
}
