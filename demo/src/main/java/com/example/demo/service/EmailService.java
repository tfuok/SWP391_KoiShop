package com.example.demo.service;

import com.example.demo.model.EmailDetails;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import org.thymeleaf.context.Context;

@Service
public class EmailService {
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    JavaMailSender mailSender;

    public void sendEmail(EmailDetails emailDetails) {
        try {
            Context context = new Context();
            String template = templateEngine.process("welcome-template", context);

            //Creating a simple mail message
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            //setting up necessary details
            mimeMessageHelper.setFrom("phuocnntse182664@fpt.edu.vn");
            mimeMessageHelper.setTo(emailDetails.getReceiver().getEmail());
            mimeMessageHelper.setText(template, true);
            mimeMessageHelper.setSubject(emailDetails.getSubject());
            mailSender.send(mimeMessage);
        }catch (MessagingException e){
            System.out.println("Error sent mail");
        }
    }
}
