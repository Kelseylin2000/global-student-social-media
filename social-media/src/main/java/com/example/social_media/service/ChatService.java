package com.example.social_media.service;

import com.example.social_media.dto.chat.ChatSessionDto;
import com.example.social_media.dto.chat.MessageDto;
import com.example.social_media.model.document.Message;

import java.util.List;

public interface ChatService {
    void saveMessage(Message message);
    List<ChatSessionDto> getChatSessions();
    List<MessageDto> getChatHistory(String chatId, int page, int size);
    boolean isParticipant(Long userId, String chatId);
}
