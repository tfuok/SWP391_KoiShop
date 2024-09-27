package com.example.demo.api;

import com.example.demo.entity.Account;
import com.example.demo.model.AccountResponse;
import com.example.demo.model.LoginRequest;
import com.example.demo.model.RegisterRequest;
import com.example.demo.service.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@SecurityRequirement(name = "api") //bat buoc phai co
public class AuthenticationAPI {
    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("register")
    public ResponseEntity register(@Valid @RequestBody RegisterRequest registerRequest) {
        AccountResponse newAccount = authenticationService.register(registerRequest);
        return ResponseEntity.ok(newAccount);
    }

    @PostMapping("login")
    public ResponseEntity login(@Valid @RequestBody LoginRequest loginRequest) {
        AccountResponse newAccount = authenticationService.login(loginRequest);
        return ResponseEntity.ok(newAccount);
    }

    @GetMapping
    public ResponseEntity getAllAccount(){
        List<Account> accounts = authenticationService.getAllAccount();
        return ResponseEntity.ok(accounts);
    }
}
