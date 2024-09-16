package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.exception.DuplicatedEntity;
import com.example.demo.model.LoginRequest;
import com.example.demo.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AuthenticationService {
    @Autowired
    AccountRepository accountRepository;

    public Account register(Account account) {
        try {
            Account newAccount = accountRepository.save(account);
            return newAccount;
        } catch (Exception e) {
            if (e.getMessage().contains(account.getCode())) {
                throw new DuplicatedEntity("Duplicate code!");
            } else if (e.getMessage().contains(account.getEmail())) {
                throw new DuplicatedEntity("Duplicate email!");
            } else {
                throw new DuplicatedEntity("Duplicate phone");
            }
        }
    }


    public List<Account> getAllAccount() {
        List<Account> accounts = accountRepository.findAll();
        return accounts;
    }

    public Account login(LoginRequest loginRequest) throws AccountNotFoundException {
        return null;
    }
}
