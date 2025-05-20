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
        message.setSubject("Luna 회원가입 인증코드입니다.");
        message.setText("인증코드: " + code);
        mailSender.send(message);
    }

    @Async
    public void sendPasswordResetEmail(String to, String link) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Luna 비밀번호 변경 링크입니다.");
            String htmlContent = "<p>2시간 동안만 유효하며 이후엔 다시 요청해야 합니다.</p>" + "<p><a href='" + link + "' target=_blank rel='noreferrer noopener'>비밀번호 변경 링크</a></p>";
            helper.setText(htmlContent, true);  // 두 번째 인자는 HTML 여부 (true)
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 중 오류 발생", e);
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

            helper.setSubject("Luna 주문코드 : " + orderId);
            String htmlContent = "<p>Luna Hair Shop을 이용해주셔서 감사드립니다.</p><p>" + orderId + "</p><p>주문내역 입니다. 상세한 내용을 보려면 링크를 클릭해주세요.</p>" + "<p><a href='" + link + "' target=_blank rel='noreferrer noopener'>구매 내역 링크</a></p><p>상품을 받으신 후에는 주문 내역에 들어가서 배송완료 버튼을 꼭 눌러주세요. 누르지 않을 시 2주 뒤 배송완료 처리 됩니다. 감사합니다.</p>";
            helper.setText(htmlContent, true);  // 두 번째 인자는 HTML 여부 (true)
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 중 오류 발생", e);
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

            helper.setSubject("Luna 주문 취소 / 주문번호 " + orderId);
            String htmlContent = "<p>Luna Hair Shop을 이용해주셔서 감사드립니다.</p><p>" + orderId + "</p><p>주문이 취소되었습니다. 상세한 내용을 보려면 링크를 클릭해주세요.</p>" + "<p><a href='" + link + "' target=_blank rel='noreferrer noopener'>구매 내역 링크</a></p>";
            helper.setText(htmlContent, true);  // 두 번째 인자는 HTML 여부 (true)
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 중 오류 발생", e);
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

            helper.setSubject("Luna 반품 신청 / 주문번호 " + orderId);
            String htmlContent = "<p>Luna Hair Shop을 이용해주셔서 감사드립니다.</p><p>" + orderId + "</p><p>반품 신청되었습니다. 상세한 내용을 보려면 링크를 클릭해주세요.</p>" + "<p><a href='" + link + "' target=_blank rel='noreferrer noopener'>구매 내역 링크</a></p>";
            helper.setText(htmlContent, true);  // 두 번째 인자는 HTML 여부 (true)
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 중 오류 발생", e);
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

            helper.setSubject("Luna 반품 취소 / 주문번호 " + orderId);
            String htmlContent = "<p>Luna Hair Shop을 이용해주셔서 감사드립니다.</p><p>" + orderId + "</p><p>반품이 취소되었습니다. 상세한 내용을 보려면 링크를 클릭해주세요.</p>" + "<p><a href='" + link + "' target=_blank rel='noreferrer noopener'>구매 내역 링크</a></p>";
            helper.setText(htmlContent, true);  // 두 번째 인자는 HTML 여부 (true)
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 중 오류 발생", e);
        }
    }
}