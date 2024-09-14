package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.exception.DuplicatedEntity;
import com.example.demo.model.LoginRequest;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AuthenticationService {
    List<Account> accounts = new ArrayList<>();
    Account account = new Account();

    public Account register(Account newAccount) {
        if (!newAccount.getEmail().equalsIgnoreCase(account.getEmail())) {
            throw new DuplicatedEntity("Duplicated Email");
        } else {
            account.setSignUpDate(new Date());
            accounts.add(newAccount);
            return newAccount;
        }
    }

    public Account login(LoginRequest loginRequest) throws AccountNotFoundException {
        if (loginRequest.getEmail().equalsIgnoreCase(account.getEmail())
                && loginRequest.getPassword().equalsIgnoreCase(account.getPassword())) {
            return account;
        } else {
            throw new AccountNotFoundException("Not found");
        }
    }
}
