package com.example.social_media.service;

import com.example.social_media.model.document.ChatSession;
import com.example.social_media.model.document.Message;

import java.util.List;

public interface ChatService {
    void saveMessage(Message message);
    List<ChatSession> getChatSessions(Long userId);
    List<Message> getChatHistory(String chatId, int page, int size);
    boolean isParticipant(Long userId, String chatId);
}
