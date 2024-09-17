package com.example.social_media.repository.mongo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.social_media.model.document.ChatSession;

public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {
    Optional<ChatSession> findByChatId(String chatId);
    List<ChatSession> findByParticipantsContaining(Long userId);
}