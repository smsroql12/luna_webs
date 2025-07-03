package com.example.luna.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("This is the Luna authentication code.");
        message.setText("Authentication code: " + code);
        mailSender.send(message);
    }

    @Async
    public void sendPasswordResetEmail(String to, String link) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Link to change Luna password.");
            String htmlContent = "<p>It's only valid for two hours, and you'll have to request it again after that.</p>" + "<p><a href='" + link + "' target=_blank rel='noreferrer noopener'>Link to Change Password</a></p>";
            helper.setText(htmlContent, true);  // 두 번째 인자는 HTML 여부 (true)
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending email.", e);
        }
    }

    @Async
    public void orderEmail(String to, String link) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);

            String[] parts = link.split("orderid=");
            String orderId = "";
            if (parts.length > 1) {
                orderId = parts[1];
            }

            helper.setSubject("Luna | Order ID : " + orderId);
            String htmlContent = "<p>Thank you for using the Luna Hair Shop.</p><p>" + orderId + "</p><p>This is the order details, please click the link for more information.</p>" + "<p><a href='" + link + "' target=_blank rel='noreferrer noopener'>Link to Purchase History</a></p><p>Once you receive the item, please enter the order details and press the delivery complete button. If you don't press it, the delivery will be completed in two weeks. Thank you.</p>";
            helper.setText(htmlContent, true);  // 두 번째 인자는 HTML 여부 (true)
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending email.", e);
        }
    }

    @Async
    public void cancelEmail(String to, String link) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);

            String[] parts = link.split("orderid=");
            String orderId = "";
            if (parts.length > 1) {
                orderId = parts[1];
            }

            helper.setSubject("Luna | [Order Cancel] | Order ID : " + orderId);
            String htmlContent = "<p>Thank you for using the Luna Hair Shop.</p><p>" + orderId + "</p><p>Your order has been canceled. Please click on the link to learn more.</p>" + "<p><a href='" + link + "' target=_blank rel='noreferrer noopener'>Link to Purchase History</a></p>";
            helper.setText(htmlContent, true);  // 두 번째 인자는 HTML 여부 (true)
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending email.", e);
        }
    }

    @Async
    public void returnEmail(String to, String link) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);

            String[] parts = link.split("orderid=");
            String orderId = "";
            if (parts.length > 1) {
                orderId = parts[1];
            }

            helper.setSubject("Luna | [Request Return] / Order ID : " + orderId);
            String htmlContent = "<p>Thank you for using the Luna Hair Shop.</p><p>" + orderId + "</p><p>Your return has been requested. Please click the link to learn more.</p>" + "<p><a href='" + link + "' target=_blank rel='noreferrer noopener'>Link to Purchase History</a></p>";
            helper.setText(htmlContent, true);  // 두 번째 인자는 HTML 여부 (true)
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending email.", e);
        }
    }

    @Async
    public void returnCancelEmail(String to, String link) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);

            String[] parts = link.split("orderid=");
            String orderId = "";
            if (parts.length > 1) {
                orderId = parts[1];
            }

            helper.setSubject("Luna : [Cancel Return] / Order ID : " + orderId);
            String htmlContent = "<p>Thank you for using the Luna Hair Shop.</p><p>" + orderId + "</p><p>Your return has been canceled. Please click the link to learn more.</p>" + "<p><a href='" + link + "' target=_blank rel='noreferrer noopener'>Link to Purchase History</a></p>";
            helper.setText(htmlContent, true);  // 두 번째 인자는 HTML 여부 (true)
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending email.", e);
        }
    }
}