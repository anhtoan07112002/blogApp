package com.blogApp.blogauth.dto.request;

import com.blogApp.blogcommon.validation.PasswordMatches;
import com.blogApp.blogcommon.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@PasswordMatches(field = "password", fieldMatch = "confirmPassword", message = "Passwords don't match")
public class ResetPasswordRequest {
    @NotBlank
    private String token;

    @NotBlank
    @StrongPassword
    private String password;

    @NotBlank
    private String confirmPassword;
}