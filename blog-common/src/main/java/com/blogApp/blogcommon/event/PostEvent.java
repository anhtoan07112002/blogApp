package com.blogApp.blogcommon.event;

import com.blogApp.blogcommon.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostEvent {
    public enum Type {
        CREATED,
        UPDATED,
        DELETED,
        PUBLISHED,
        LIKED,
        COMMENTED
    }

    private Long postId;
    private Long authorId;
    private Type type;
    private PostStatus status;
    private String title;
}