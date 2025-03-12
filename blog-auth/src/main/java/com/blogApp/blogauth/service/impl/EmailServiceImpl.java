package com.blogApp.blogauth.service.impl;

import com.blogApp.blogauth.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendPasswordResetEmail(String to, String token, String firstName) {
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("firstName", firstName);
            context.setVariable("token", token);
            context.setVariable("expiryHours", 24);

            String content = templateEngine.process("password-reset-email", context);
            sendEmail(to, "Đặt lại mật khẩu", content);
            
            log.info("Đã gửi email reset password đến: {}", to);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email reset password: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email reset password", e);
        }
    }

    @Override
    public void sendRegistrationConfirmationEmail(String to, String firstName) {
        try {
            Context context = new Context(Locale.getDefault());
            context.setVariable("firstName", firstName);

            String content = templateEngine.process("registration-confirmation-email", context);
            sendEmail(to, "Xác nhận đăng ký tài khoản", content);
            
            log.info("Đã gửi email xác nhận đăng ký đến: {}", to);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email xác nhận đăng ký: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email xác nhận đăng ký", e);
        }
    }

    private void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }
} 