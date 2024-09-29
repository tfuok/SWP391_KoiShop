package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.exception.DuplicatedEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.AccountResponse;
import com.example.demo.model.EmailDetails;
import com.example.demo.model.LoginRequest;
import com.example.demo.model.RegisterRequest;
import com.example.demo.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthenticationService implements UserDetailsService {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    TokenService tokenService;
    @Autowired
    EmailService emailService;

    public AccountResponse register(RegisterRequest registerRequest) {
        Account account = modelMapper.map(registerRequest, Account.class);
        if (accountRepository.existsByEmail(account.getEmail())) {
            throw new DuplicatedEntity("Email existed!");
        }
        if (accountRepository.existsByPhone(account.getPhone())) {
            throw new DuplicatedEntity("Phone existed!");
        }
        try {
            String originPassword = account.getPassword();
            account.setPassword(passwordEncoder.encode(originPassword));
            Account newAccount = accountRepository.save(account);
            //sau khi dang ki thanh cong thi se gui mail ve cho nguoi dung
            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setReceiver(newAccount);
            emailDetails.setSubject("Welcome to KoiShop");
            emailDetails.setLink("github.com");
            emailService.sendEmail(emailDetails);
            return modelMapper.map(newAccount, AccountResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while register!");
        }
    }


    public List<Account> getAllAccount() {
        List<Account> accounts = accountRepository.findAccountByIsDeletedFalse();
        return accounts;
    }

    public AccountResponse login(LoginRequest loginRequest) {
        //        //xử lý logic liên quan đến login
//        //2 trường hợp xảy ra
//        /*
//         *   1. tồn tại
//         *   2.ko tồn tại
//         */
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            ));
            //=> tài khoản có tồn tại
            Account account = (Account) authentication.getPrincipal();
            AccountResponse accountResponse = modelMapper.map(account, AccountResponse.class);
            accountResponse.setToken(tokenService.generateToken(account));
            return accountResponse;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EntityNotFoundException("Email or password invalid!");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return (UserDetails) accountRepository.findAccountByEmail(email);
    }

    public Account getCurrentAccount() {
        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findAccountById(account.getId());
    }

    public Account deleteAccount(long id) {
        Account account = accountRepository.findAccountById(id);
        if (account == null) {
            throw new NotFoundException("Account not found!");
        }
        account.setDeleted(true);
        return accountRepository.save(account);
    }

    public Account updateAccount(RegisterRequest registerRequest, long id) {
        Account account = accountRepository.findAccountById(id);
        if (account == null) {
            throw new NotFoundException("Account not exist!");
        }
        // Kiểm tra email,số điện thoại đã tồn tại chưa (ngoại trừ tài khoản hiện tại)
        if (!account.getEmail().equals(registerRequest.getEmail()) && accountRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicatedEntity("Email already exists!");
        }
        if (!account.getPhone().equals(registerRequest.getPhone()) && accountRepository.existsByPhone(registerRequest.getPhone())) {
            throw new DuplicatedEntity("Phone number already exists!");
        }
        account.setEmail(registerRequest.getEmail());
        account.setUsername(registerRequest.getUsername());
        account.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        account.setPhone(registerRequest.getPhone());
        return accountRepository.save(account);
    }

}
