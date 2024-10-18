package com.example.demo.model.Request;

import com.example.demo.entity.Account;
import lombok.Data;

@Data
public class EmailDetails {
    private Account receiver; // The account who will receive the email
    private String subject;   // The subject of the email
    private String link;      // The link included in the email (e.g., home page link)
    private String password;  // The password to be included in the email
}
