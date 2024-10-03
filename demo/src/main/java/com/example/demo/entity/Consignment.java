package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Consignment implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long consignmentID;

    String type;

    String address;

    String description;

    String cost;

    Date StartDate;

    Date EndDate;

    Date createDate;

    String status = "pending";

    Boolean isDeleted = false;

    @OneToMany(mappedBy = "consignment")
    @JsonIgnore
    List<Koi> kois;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;

    @ManyToOne
    @JoinColumn(name = "care_type_id")
    CareType careType;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }
}