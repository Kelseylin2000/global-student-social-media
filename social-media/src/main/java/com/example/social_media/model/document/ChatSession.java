package com.example.social_media.model.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_sessions")
public class ChatSession {

    @Id
    private String id;
    
    @Indexed
    private String chatId;
    private List<Long> participants;
    private Message latestMessage;
}