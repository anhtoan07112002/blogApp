package com.blogApp.blogcommon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private String role;
    private Long postsCount;
    private Long followersCount;
    private Long followingCount;
}
