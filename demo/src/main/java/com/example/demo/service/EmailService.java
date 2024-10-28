package com.example.demo.service;

import com.example.demo.entity.Consignment;
import com.example.demo.entity.Orders;
import com.example.demo.model.Request.EmailDetails;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import org.thymeleaf.context.Context;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

@Service
public class EmailService {
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    JavaMailSender mailSender;

    public void sendEmail(EmailDetails emailDetails, String templateName) {
        try {
            Context context = new Context();
            context.setVariable("name", emailDetails.getReceiver().getUsername());
            context.setVariable("email", emailDetails.getReceiver().getEmail());
            context.setVariable("password", emailDetails.getPassword()); // Add password to the context
            context.setVariable("button", "Go to home page");
            context.setVariable("link", emailDetails.getLink());


            String template = templateEngine.process(templateName, context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

            mimeMessageHelper.setFrom("phuocnntse182664@fpt.edu.vn");
            mimeMessageHelper.setTo(emailDetails.getReceiver().getEmail());
            mimeMessageHelper.setSubject(emailDetails.getSubject());
            mimeMessageHelper.setText(template, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println("Error sending email: " + e.getMessage());
        }
    }

    public void sendEmailWithAttachment(EmailDetails emailDetails, byte[] pdfBytes) {
        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);


            helper.setFrom("phuocnntse182664@fpt.edu.vn");
            helper.setTo(emailDetails.getReceiver().getEmail());
            helper.setSubject(emailDetails.getSubject());
            helper.setText("Please find your Koi Certificate attached.");


            helper.addAttachment("Koi_Certificate.pdf", new ByteArrayDataSource(pdfBytes, "application/pdf"));


            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendOrderBillEmail(Orders order, String toEmail) {
        try {
            Context context = new Context();
            context.setVariable("name", order.getCustomer().getUsername());
            context.setVariable("email", order.getCustomer().getEmail());
            context.setVariable("orderDate", order.getDate());
            context.setVariable("orderDescription", order.getDescription());
            context.setVariable("orderDetails", order.getOrderDetails());

            // Calculate the total for individual items
            double total = order.getOrderDetails().stream()
                    .mapToDouble(detail -> detail.getPrice())
                    .sum();
            context.setVariable("total", order.getTotal()); // Set total
            context.setVariable("grandTotal", order.getFinalAmount());
            String template = templateEngine.process("bill", context);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom("phuocnntse182664@fpt.edu.vn");
            mimeMessageHelper.setTo(toEmail);
            mimeMessageHelper.setSubject("Your Order Bill");
            mimeMessageHelper.setText(template, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.out.println("Error sending email: " + e.getMessage());
        }
    }
    public void sendConsignmentBillEmail(Consignment consignment, String toEmail) {
        try {
            Context context = new Context();
            context.setVariable("billingDate", consignment.getCreateDate());
            context.setVariable("bill", consignment);
            context.setVariable("consignment", consignment);


            context.setVariable("bill.cost", consignment.getCost());
            context.setVariable("bill.id", consignment.getId());

            String template = templateEngine.process("consignment-bill", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

            mimeMessageHelper.setFrom("phuocnntse182664@fpt.edu.vn");
            mimeMessageHelper.setTo(toEmail);
            mimeMessageHelper.setSubject("Your Consignment Bill");
            mimeMessageHelper.setText(template, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.out.println("Error sending email: " + e.getMessage());
        }
    }
}


