package com.example.luna.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Luna 회원가입 인증코드입니다.");
        message.setText("인증코드: " + code);
        mailSender.send(message);
    }

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
}