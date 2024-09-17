package com.example.social_media.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.social_media.model.document.Message;
import com.example.social_media.service.ChatService;

import java.time.LocalDateTime;

@Controller
public class WebSocketChatController {
    
    private SimpMessagingTemplate messagingTemplate;
    private ChatService chatService;

    public WebSocketChatController(SimpMessagingTemplate messagingTemplate, ChatService chatService){
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Message message) {

        message.setCreatedAt(LocalDateTime.now());

        chatService.saveMessage(message);

        messagingTemplate.convertAndSendToUser(
            message.getReceiverId().toString(),
            "/queue/messages",
            message
        );
    }
}