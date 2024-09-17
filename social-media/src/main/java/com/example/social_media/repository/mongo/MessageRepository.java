package com.example.social_media.repository.mongo;

import com.example.social_media.model.document.Message;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByChatIdOrderByCreatedAtDesc(String chatId, Pageable pageable);
}