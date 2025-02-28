package com.blogApp.blogcommon.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    public enum Type {
        CREATED,
        UPDATED,
        DELETED,
        PASSWORD_CHANGED,
        EMAIL_VERIFIED
    }

    private Long userId;
    private Type type;
    private String email;
    private String username;
}
