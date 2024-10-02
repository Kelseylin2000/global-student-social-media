package com.example.social_media.service.implement;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.social_media.dto.chat.ChatSessionDto;
import com.example.social_media.dto.chat.MessageDto;
import com.example.social_media.model.document.ChatSession;
import com.example.social_media.model.document.Message;
import com.example.social_media.model.entity.User;
import com.example.social_media.repository.mongo.ChatSessionRepository;
import com.example.social_media.repository.mongo.MessageRepository;
import com.example.social_media.repository.mysql.UserRepository;
import com.example.social_media.service.AuthService;
import com.example.social_media.service.ChatService;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Comparator;


@Service
public class ChatServiceImpl implements ChatService{

    private final MessageRepository messageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    public ChatServiceImpl(MessageRepository messageRepository, ChatSessionRepository chatSessionRepository, UserRepository userRepository, AuthService authService){
        this.messageRepository = messageRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.userRepository = userRepository;
        this.authService = authService;
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
    public List<ChatSessionDto> getChatSessions() {
    
        Long userId = authService.getCurrentUserId();
    
        String currentUserName = userRepository.findById(userId)
            .map(User::getName)
            .orElse("User");
    
        List<ChatSession> sessions = chatSessionRepository.findByParticipantsContaining(userId);
        
        sessions.sort(Comparator.comparing(session -> session.getLatestMessage().getCreatedAt(), Comparator.reverseOrder()));
    
        return sessions.stream()
            .map(session -> convertToDto(session, currentUserName, userId))
            .collect(Collectors.toList());
    }

    @Override
    public List<MessageDto> getChatHistory(String chatId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Message> messages = messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable);
        return messages.stream()
                .map(this::convertToMessageDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isParticipant(Long userId, String chatId) {
        return chatSessionRepository.findByChatId(chatId)
                .map(chatSession -> chatSession.getParticipants().contains(userId))
                .orElse(false);
    }

    private ChatSessionDto convertToDto(ChatSession session, String currentUserName, Long currentUserId) {

        List<Long> participantIds = session.getParticipants();
        List<String> participantNames = new ArrayList<>();

        for (Long participantId : participantIds) {
            if (participantId.equals(currentUserId)) {
                participantNames.add(currentUserName);
            } else {
                User user = userRepository.findById(participantId)
                        .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + participantId));
                participantNames.add(user.getName());
            }
        }

        MessageDto latestMessageDto = convertToMessageDto(session.getLatestMessage());

        return new ChatSessionDto(
                session.getChatId(),
                participantIds,
                participantNames,
                latestMessageDto
        );
    }


    private MessageDto convertToMessageDto(Message message) {
        return new MessageDto(
                message.getChatId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
