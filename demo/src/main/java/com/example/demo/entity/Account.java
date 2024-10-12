package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Account implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    String username;

    @Email(message = "Email not valid")
    String email;

    @Pattern(regexp = "(84|0[3|5|7|8|9])+([0-9]{8})\\b", message = "Invalid phone number!")
    String phone;

    @NotBlank(message = "Password must not be blank!")
    @Size(min = 6, message = "Password must at least 6 character!")
    String password;

    Date signUpDate = new Date();

    boolean isDeleted = false;

    String address;

    @Enumerated(EnumType.STRING)
    Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (this.role != null) authorities.add(new SimpleGrantedAuthority(this.role.toString()));
        return authorities;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
//
//    @OneToMany(mappedBy = "account")
//    @JsonIgnore
//    Set<Voucher> vouchers = new HashSet<>();


    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    Set<Orders> orders;

    @OneToMany(mappedBy = "account")
            @JsonIgnore
    Set<Koi> koiLots;

    @OneToMany(mappedBy = "from")
    List<Transactions> transactionsFrom;

    @OneToMany(mappedBy = "to")
    List<Transactions> transactionsTo;

    double balance = 0;
}
