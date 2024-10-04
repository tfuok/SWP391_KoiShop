package com.example.demo.api;

import com.example.demo.entity.Account;
import com.example.demo.model.Request.ForgotPasswordRequest;
import com.example.demo.model.Request.LoginRequest;
import com.example.demo.model.Request.RegisterRequest;
import com.example.demo.model.Request.ResetPasswordRequest;
import com.example.demo.model.Response.AccountResponse;
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
    public ResponseEntity getAllAccount() {
        List<Account> accounts = authenticationService.getAllAccount();
        return ResponseEntity.ok(accounts);
    }

    @DeleteMapping("/account/{id}")
    public ResponseEntity deleteAccount(@Valid @PathVariable long id) {
        Account account = authenticationService.deleteAccount(id);
        return ResponseEntity.ok(account);
    }

    @PutMapping("/account/{id}")
    public ResponseEntity updateAccount(@Valid @RequestBody RegisterRequest request, @PathVariable long id) {
        Account newKoi = authenticationService.updateAccount(request, id);
        return ResponseEntity.ok(newKoi);
    }

    @GetMapping("/account/{id}")
    public ResponseEntity getAccountByID(Long id) {
        Account account = authenticationService.searchByID(id);
        return ResponseEntity.ok(account);
    }

    @PostMapping("forgot-password")
    public ResponseEntity forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest){
        authenticationService.forgotPassword(forgotPasswordRequest);
        return ResponseEntity.ok("Email sent!");
    }

    @PostMapping("reset-password")
    public ResponseEntity resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest){
        authenticationService.resetPassword(resetPasswordRequest);
        return ResponseEntity.ok("Reset successfully");
    }
}
