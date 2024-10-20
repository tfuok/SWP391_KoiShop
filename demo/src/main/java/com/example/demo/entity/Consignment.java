package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
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
    long id;

    Type type;

    String address;

    String description;

    float cost;

    Date StartDate;

    Date EndDate;

    Date createDate;


    @Enumerated(EnumType.STRING)  // Use EnumType.STRING to match ENUM in database
    private Status status;

    Boolean isDeleted = false;
    @OneToMany(mappedBy = "consignment",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsignmentDetails> consignmentDetails = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "care_type_id")
    private CareType careType;


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
    @OneToOne(mappedBy = "consignment")
    @JsonIgnore
    private Payment payment;
}