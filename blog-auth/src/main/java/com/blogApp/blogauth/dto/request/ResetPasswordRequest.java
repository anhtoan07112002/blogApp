package com.blogApp.blogauth.dto.request;

import com.blogApp.blogcommon.validation.PasswordMatches;
import com.blogApp.blogcommon.validation.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@PasswordMatches(field = "password", fieldMatch = "confirmPassword", message = "Passwords don't match")
@Schema(description = "Yêu cầu reset mật khẩu")
public class ResetPasswordRequest {
    @NotBlank
    @Schema(description = "Token xác nhận reset mật khẩu (từ email)", example = "a1b2c3d4-5678-90ab-cdef-ghijklmnopqr", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @NotBlank
    @StrongPassword
    @Schema(description = "Mật khẩu mới (phải đáp ứng yêu cầu về độ mạnh)", example = "NewPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank
    @Schema(description = "Xác nhận mật khẩu mới (phải trùng với mật khẩu mới)", example = "NewPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;
}