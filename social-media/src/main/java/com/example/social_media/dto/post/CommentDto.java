package com.example.social_media.dto.post;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private String commentId;
    private Long userId;
    private String name;
    private String content;
    private LocalDateTime timestamp;
}
