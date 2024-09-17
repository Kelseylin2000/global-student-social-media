package com.example.social_media.model.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posts")
public class Post {
    @Id
    private String id;

    @Indexed
    private String postId;
    @Indexed
    private Long userId;
    private String content;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> tags;
    private Long views;
    private Long saveCount;
    private List<Comment> comments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Comment {
        private String commentId;
        private Long userId;
        private String content;
        private LocalDateTime timestamp;
    }
}