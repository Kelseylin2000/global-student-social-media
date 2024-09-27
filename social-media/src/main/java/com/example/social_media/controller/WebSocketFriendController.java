package com.example.social_media.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.example.social_media.dto.friend.FriendRequestDto;
import com.example.social_media.dto.friend.UserFriendResultDto;

import com.example.social_media.service.FriendService;

@Controller
public class WebSocketFriendController {

    private SimpMessagingTemplate messagingTemplate;
    private FriendService friendService;

    public WebSocketFriendController(SimpMessagingTemplate messagingTemplate, FriendService friendService) {
        this.messagingTemplate = messagingTemplate;
        this.friendService = friendService;
    }

    @MessageMapping("/friend.sendRequest")
    public void sendFriendRequest(@Payload FriendRequestDto friendRequest) {

        UserFriendResultDto senderInfo = friendService.sendFriendRequest(friendRequest.getUserId(), friendRequest.getTargetUserId());

        messagingTemplate.convertAndSendToUser(
            friendRequest.getTargetUserId().toString(),
            "/queue/friend-requests",
            senderInfo
        );
    }

    @MessageMapping("/friend.acceptRequest")
    public void acceptFriendRequest(@Payload FriendRequestDto friendRequest) {

        UserFriendResultDto accepterInfo = friendService.acceptFriendRequest(friendRequest.getUserId(), friendRequest.getTargetUserId());

        messagingTemplate.convertAndSendToUser(
            friendRequest.getTargetUserId().toString(),
            "/queue/friend-accept",
            accepterInfo
        );
    }
}
