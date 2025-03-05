package com.blogApp.blogcommon.config;

import com.blogApp.blogcommon.dto.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
@Slf4j
public class AuditConfig {
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Kiểm tra authentication có tồn tại và đã xác thực hay không
            if (authentication == null || !authentication.isAuthenticated() ||
                    authentication instanceof AnonymousAuthenticationToken) {
                return Optional.empty();
            }

            // Lấy principal và kiểm tra kiểu dữ liệu
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserPrincipal) {
                // Trường hợp principal là UserPrincipal (đã đăng nhập)
                return Optional.of(((UserPrincipal) principal).getId());
            } else if (principal instanceof String) {
                log.debug("no user name");
                // Trường hợp principal là String (thường là username hoặc token)
                // Không thể ép kiểu trực tiếp sang UserPrincipal
                // Trong trường hợp này, ta return empty để không có lỗi
                return Optional.empty();
            } else {
                // Các trường hợp khác
                return Optional.empty();
            }
        };
    }
}
