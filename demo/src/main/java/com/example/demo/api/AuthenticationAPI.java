package com.example.demo.api;

import com.example.demo.entity.Account;
import com.example.demo.entity.Role;
import com.example.demo.model.Request.*;
import com.example.demo.model.Response.AccountResponse;
import com.example.demo.service.AuthenticationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @DeleteMapping("/account/{id}")
    public ResponseEntity deleteAccount(@Valid @PathVariable long id) {
        Account account = authenticationService.deleteAccount(id);
        return ResponseEntity.ok(account);
    }

    @PutMapping("/account/{id}")
    public ResponseEntity updateAccount(@Valid @RequestBody UpdateRequest request, @PathVariable long id) {
        Account newKoi = authenticationService.updateAccount(request, id);
        return ResponseEntity.ok(newKoi);
    }


    @PostMapping("forgot-password")
    public ResponseEntity forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        authenticationService.forgotPassword(forgotPasswordRequest);
        return ResponseEntity.ok("Email sent!");
    }

    @PostMapping("reset-password")
    public ResponseEntity resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        authenticationService.resetPassword(resetPasswordRequest);
        return ResponseEntity.ok("Reset successfully");
    }

//    // Endpoint to get accounts by role
//    @GetMapping("/account/role/{role}")
//    public ResponseEntity<List<Account>> getAccountByRole(@PathVariable("role") Role role) {
//        List<Account> accounts = authenticationService.getAccountByRole(role);
//        return ResponseEntity.ok(accounts);
//    }

    // Endpoint to get accounts by name where role is STAFF
    @GetMapping("/account/name/{name}")
    public ResponseEntity<List<Account>> getAccountsByNameAndRoleStaff(@PathVariable("name") String name) {
        List<Account> accounts = authenticationService.getAccountsByNameAndRoleStaff(name);
        return ResponseEntity.ok(accounts);
    }

    //    @GetMapping("/account/{id}")
//    public ResponseEntity getAccountByID(@PathVariable long id) {
//        Account account = authenticationService.searchByID(id);
//        return ResponseEntity.ok(account);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<Account>> getAllAccount() {
//        List<Account> accounts = authenticationService.getAllAccount();
//        return ResponseEntity.ok(accounts);
//    }
    @GetMapping("/account")
    public ResponseEntity<?> getAccounts(
            @RequestParam(value = "role", required = false) Role role,
            @RequestParam(value = "id", required = false) Long id) {

        // Nếu có id và role cùng lúc, kiểm tra xem id và role có trùng khớp
        if (id != null && role != null) {
            Account account = authenticationService.searchByID(id);
            if (account == null || !account.getRole().equals(role)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not exist");
            }
            return ResponseEntity.ok(account);
        }

        // Nếu chỉ có id, tìm account theo id
        if (id != null) {
            Account account = authenticationService.searchByID(id);
            if (account == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
            }
            return ResponseEntity.ok(account);
        }

        // Nếu chỉ có role, tìm các account theo role
        if (role != null) {
            List<Account> accountsByRole = authenticationService.getAccountByRole(role);
            if (accountsByRole.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No accounts found with the specified role");
            }
            return ResponseEntity.ok(accountsByRole);
        }

        // Nếu không có điều kiện, lấy tất cả các account
        List<Account> allAccounts = authenticationService.getAllAccount();
        return ResponseEntity.ok(allAccounts);
    }


}
