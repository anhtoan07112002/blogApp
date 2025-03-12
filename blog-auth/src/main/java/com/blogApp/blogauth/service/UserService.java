package com.blogApp.blogauth.service;

import com.blogApp.blogauth.dto.request.ChangePasswordRequest;
import com.blogApp.blogauth.dto.request.ResetPasswordRequest;
import com.blogApp.blogauth.model.PasswordResetToken;
import com.blogApp.blogauth.repository.PasswordResetTokenRepository;
import com.blogApp.blogauth.repository.UserRepository;
import com.blogApp.blogcommon.constant.AppConstants;
import com.blogApp.blogcommon.dto.response.UserProfile;
import com.blogApp.blogcommon.exception.BadRequestException;
import com.blogApp.blogcommon.exception.ResourceNotFoundException;
import com.blogApp.blogcommon.model.User;
import com.blogApp.blogcommon.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final CacheService cacheService;
    private final EmailService emailService;

    public UserProfile getUserProfile(String username) {

        Object cacheProfile = cacheService.get(AppConstants.USER_PROFILE_CACHE, username);

        if (cacheProfile != null) {
            return (UserProfile) cacheProfile;
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        UserProfile userProfile = UserProfile.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .updateAt(user.getUpdatedAt())
                .role(user.getRole().name())
                .build();

        cacheService.set(AppConstants.USER_PROFILE_CACHE, username, userProfile, 30, TimeUnit.MINUTES);
        return userProfile;
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if old password is correct
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Update with new password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Hủy token cũ nếu có
        resetTokenRepository.findByUser(user).forEach(resetTokenRepository::delete);

        // Tạo token mới
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(Instant.now().plusSeconds(86400)); // 24 giờ
        resetTokenRepository.save(resetToken);

        // Gửi email reset password
        emailService.sendPasswordResetEmail(user.getEmail(), token, user.getFirstName());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token"));

        if (resetToken.isExpired()) {
            resetTokenRepository.delete(resetToken);
            throw new BadRequestException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // Xóa token đã sử dụng
        resetTokenRepository.delete(resetToken);
    }
}
