package com.example.social_media.dto.post;

import java.util.List;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostWithoutCommentsDto {

    private String postId;

    private Long userId;
    private String name;
    private String phase;
    private String fromSchoolName;
    private String toSchoolName;

    private String content;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> tags;
    private Long views;
    private Long commentCount;
}
