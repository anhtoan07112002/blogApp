package com.blogApp.blogcommon.event;

import com.blogApp.blogcommon.dto.CommentSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event được phát khi có sự kiện liên quan đến bình luận
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEvent {
    public enum EventType {
        CREATED,
        UPDATED,
        APPROVED,
        REJECTED,
        DELETED
    }

    private UUID commentId;
    private UUID postId;
    private String authorId;
    private EventType eventType;
    private LocalDateTime eventTime;
    private CommentSummaryDTO commentSummary;
}
