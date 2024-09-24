package com.example.social_media.dto.chat;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {

    private String chatId;

    private Long senderId;
    private Long receiverId;

    private String content;
    private LocalDateTime createdAt;
}
