package com.blogApp.blogauth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Phản hồi chứa thông tin token xác thực")
public class TokenResponse {
    @Schema(description = "Access token để xác thực", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "Refresh token để làm mới access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    @Schema(description = "Loại token", example = "Bearer")
    private String tokenType;
    
    @Schema(description = "Thời gian hết hạn của access token (tính bằng giây)", example = "900")
    private Long expiresIn;
}
