package com.blogApp.blogauth.service;

public interface EmailService {
    /**
     * Gửi email reset password
     * @param to Email người nhận
     * @param token Token reset password
     * @param firstName Tên người nhận
     */
    void sendPasswordResetEmail(String to, String token, String firstName);

    /**
     * Gửi email xác nhận đăng ký
     * @param to Email người nhận
     * @param firstName Tên người nhận
     */
    void sendRegistrationConfirmationEmail(String to, String firstName);
} 