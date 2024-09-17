package com.example.social_media.service.implement;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.social_media.model.document.ChatSession;
import com.example.social_media.model.document.Message;
import com.example.social_media.repository.mongo.ChatSessionRepository;
import com.example.social_media.repository.mongo.MessageRepository;
import com.example.social_media.service.ChatService;

import java.util.List;
import java.util.Arrays;
import java.util.Comparator;


@Service
public class ChatServiceImpl implements ChatService{

    private MessageRepository messageRepository;
    private ChatSessionRepository chatSessionRepository;

    public ChatServiceImpl(MessageRepository messageRepository, ChatSessionRepository chatSessionRepository){
        this.messageRepository = messageRepository;
        this.chatSessionRepository = chatSessionRepository;
    }

    @Override
    public void saveMessage(Message message) {

        messageRepository.save(message);

        String chatId = message.getChatId();
        ChatSession chatSession = chatSessionRepository.findByChatId(chatId).orElseGet(() -> {
            ChatSession newSession = new ChatSession();
            newSession.setChatId(chatId);
            newSession.setParticipants(Arrays.asList(message.getSenderId(), message.getReceiverId()));
            return newSession;
        });
        
        chatSession.setLatestMessage(message);
        chatSessionRepository.save(chatSession);
    }

    @Override
    public List<ChatSession> getChatSessions(Long userId) {
        List<ChatSession> sessions = chatSessionRepository.findByParticipantsContaining(userId);
        sessions.sort(Comparator.comparing(session -> session.getLatestMessage().getCreatedAt(), Comparator.reverseOrder()));
        return sessions;
    }

    @Override
    public List<Message> getChatHistory(String chatId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable);
    }

    @Override
    public boolean isParticipant(Long userId, String chatId) {
        return chatSessionRepository.findByChatId(chatId)
                .map(chatSession -> chatSession.getParticipants().contains(userId))
                .orElse(false);
    }
}
