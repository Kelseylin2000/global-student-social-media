package com.example.social_media.dto.chat;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionDto {

    private String chatId;
    private List<Long> participants;
    private List<String> participantsName;
    private MessageDto latestMessage;
}
