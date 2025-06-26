package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.DuplicatedEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.*;
import com.example.demo.model.Response.AccountResponse;
import com.example.demo.model.Response.LoginGoogleResponse;
import com.example.demo.model.Response.StaffResponse;
import com.example.demo.repository.AccountRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
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

        // Check if email or phone already exists
        if (accountRepository.existsByEmail(account.getEmail())) {
            throw new DuplicatedEntity("Email already exists!");
        }
        if (accountRepository.existsByPhone(account.getPhone())) {
            throw new DuplicatedEntity("Phone number already exists!");
        }

        try {
            String originPassword = registerRequest.getPassword();;

//            // Generate a random password for STAFF role
//            if (registerRequest.getRole() == Role.STAFF) {
//                originPassword = generateRandomPassword(6);  // Generate a 6-character password for STAFF
//            } else {
//                originPassword = registerRequest.getPassword();  // Use the provided password for other roles
//            }

            // Encode the password and set it in the account
            account.setPassword(passwordEncoder.encode(originPassword));

            // Save the new account to the database
            Account newAccount = accountRepository.save(account);

//            // Prepare email details with the generated password
//            EmailDetails emailDetails = new EmailDetails();
//            emailDetails.setReceiver(newAccount);
//            emailDetails.setSubject("Welcome to KoiShop");
//            emailDetails.setLink("http://koishop.site/");
//            emailDetails.setPassword(originPassword);  // Include the original (non-encoded) password in the email
//
//            // Send the email to the user with login details
//            emailService.sendEmail(emailDetails, "welcome-template");

            // Return the registered account details as a response
            return modelMapper.map(newAccount, AccountResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while registering the account!");
        }
    }



    public List<Account> getAllAccount() {
        return accountRepository.findAccountByIsDeletedFalseOrderByRole();
    }

    public AccountResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Extract the authenticated account
            Account account = (Account) authentication.getPrincipal();
            AccountResponse accountResponse = modelMapper.map(account, AccountResponse.class);
            accountResponse.setToken(tokenService.generateToken(account));

            return accountResponse;

        } catch (BadCredentialsException e) {
            throw new EntityNotFoundException("Email or password invalid!");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An unexpected error occurred");
        }
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return (UserDetails) accountRepository.findAccountByEmailAndIsDeletedFalse(email);
    }

    public Account getCurrentAccount() {
        Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findAccountById(account.getId());
    }

    public Account deleteAccount(long id) {
        Account account = accountRepository.findAccountByIdAndIsDeletedFalse(id);
        if (account == null) {
            throw new NotFoundException("Account not found!");
        }
        account.setDeleted(true);
        return accountRepository.save(account);
    }

    public Account updateAccount(UpdateRequest registerRequest, long id) {
        Account account = accountRepository.findAccountByIdAndIsDeletedFalse(id);
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
        account.setPhone(registerRequest.getPhone());
        account.setAddress(registerRequest.getAddress());
        return accountRepository.save(account);
    }

    public Account searchByID(Long id) {
        Account account = accountRepository.findAccountByIdAndIsDeletedFalse(id);
        if (account == null) {
            throw new NotFoundException("Account not exist!");
        }
        return account;
    }


    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        Account account = accountRepository.findAccountByEmailAndIsDeletedFalse(forgotPasswordRequest.getEmail());
        if (account == null) throw new NotFoundException("Account not exist");

        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setReceiver(account);
        emailDetails.setSubject("Reset Password");
        emailDetails.setLink("http://koishop.site/reset_password?token=" + tokenService.generateToken(account)); // Use the reset password link

        // Send the email
        emailService.sendEmail(emailDetails, "forgot-password");
    }

    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        Account account = getCurrentAccount();
        account.setPassword(passwordEncoder.encode(resetPasswordRequest.getPassword()));
        accountRepository.save(account);
    }
    public List<StaffResponse> getStaff() {
        List<Account> staffs = accountRepository.findStaffWithOrdersAndConsignments();
        List<StaffResponse> staffResponses = new ArrayList<>();

        for (Account account : staffs) {
            StaffResponse staffResponse = new StaffResponse();
            staffResponse.setStaff(account);
            System.out.println("Account ID: " + account.getId() + ", Orders size: " + account.getOrders().size());
            System.out.println("Account ID: " + account.getId() + ", Consignments size: " + account.getConsignments().size());
            long orderCount = account.getStaff_orders().stream()
                    .filter(orders -> orders.getStatus() != Status.CANCELLED &&
                            orders.getStatus() != Status.COMPLETED &&
                            orders.getStatus() != Status.DECLINED)
                    .count();
            staffResponse.setOrderCount(orderCount);

            long consignmentCount = account.getStaff_consignments().stream()
                    .filter(consignment -> consignment.getStatus() != Status.CONFIRMED && consignment.getStatus() != Status.DECLINED)
                    .count();
            staffResponse.setConsignmentCount(consignmentCount);
            staffResponses.add(staffResponse);
        }


        staffResponses.sort(Comparator.comparingLong(response -> response.getOrderCount() + response.getConsignmentCount()));

        return staffResponses;
    }

    public List<Account> getAccountByRole(Role role) {
        return accountRepository.findByRoleAndIsDeletedFalse(role);
    }
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}|;:,.<>?";

    private static final String ALL_CHARACTERS = UPPER + LOWER + DIGITS + SPECIAL;
    private static final SecureRandom random = new SecureRandom();

    // Method to generate random password of specified length
    public static String generateRandomPassword(int length) {
        StringBuilder password = new StringBuilder(length);

        // Ensure that password contains at least one character from each set
        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        // Fill the remaining characters randomly from all available characters
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARACTERS.charAt(random.nextInt(ALL_CHARACTERS.length())));
        }

        // Shuffle to make sure the password is unpredictable
        return shuffleString(password.toString());
    }

    // Helper method to shuffle characters in a string
    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            int randomIndex = random.nextInt(characters.length);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }
        return new String(characters);
    }

    // Method to get accounts by name where role is STAFF
    public List<Account> getAccountsByNameAndRoleStaff(String name) {
        List<Account> accounts =  accountRepository.findByUsernameContainingAndRoleAndIsDeletedFalse(name, Role.STAFF);
        if (accounts == null) throw new NotFoundException("Account not found");
        return accounts;
    }

    public LoginGoogleResponse loginGoogle(LoginGoogleRequest loginGoogleRequest) {
        try {
            // Decode Firebase token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(loginGoogleRequest.getToken());
            String email = decodedToken.getEmail();
            String username = decodedToken.getName();

            // Check if user exists or create a new one
            Account user = accountRepository.findAccountByEmailAndIsDeletedFalse(email);
            if (user == null) {
                user = new Account();
                user.setEmail(email);
                user.setRole(Role.CUSTOMER);
                user.setUsername(username);
                user = accountRepository.save(user);
            }

            // Generate a new JWT token
            String jwtToken = tokenService.generateToken(user);

            // Build the response
            return LoginGoogleResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .role(user.getRole())
                    .address(user.getAddress()) // Assuming user has an address field
                    .phone(user.getPhone()) // Assuming user has a phone field
                    .token(jwtToken)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            // Optionally, throw a custom exception or return an error response
            return null;
        }
    }
}
