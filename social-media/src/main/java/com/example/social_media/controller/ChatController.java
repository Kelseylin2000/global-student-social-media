package com.example.social_media.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.social_media.dto.ApiResponseDto;
import com.example.social_media.dto.ErrorResponseDto;
import com.example.social_media.dto.chat.ChatSessionDto;
import com.example.social_media.dto.chat.MessageDto;
import com.example.social_media.dto.user.UserDto;
import com.example.social_media.service.ChatService;

import java.util.List;


@RestController
@RequestMapping("/api/1.0/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService){
        this.chatService = chatService;
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponseDto<List<ChatSessionDto>>> getChatSessions() {

        List<ChatSessionDto> sessions = chatService.getChatSessions();
        return ResponseEntity.ok(new ApiResponseDto<>(sessions));

    }

    @GetMapping("/history")
    public ResponseEntity<?> getChatHistory(@RequestParam String chatId,
                                        @RequestParam int page,
                                        @RequestParam int size) {

        UserDto currentUser = (UserDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = currentUser.getUserId();

        if (!chatService.isParticipant(userId, chatId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body(new ErrorResponseDto("You are not authorized to view this chat history."));
        }

        List<MessageDto> messages = chatService.getChatHistory(chatId, page, size);
        return ResponseEntity.ok(new ApiResponseDto<>(messages));
    }
}
 