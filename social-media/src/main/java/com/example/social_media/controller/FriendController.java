package com.example.social_media.controller;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.social_media.dto.ApiResponseDto;
import com.example.social_media.dto.friend.PendingFriendRequestDto;
import com.example.social_media.dto.friend.UserFriendResultDto;
import com.example.social_media.service.FriendService;


@RestController
@RequestMapping("/api/1.0/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService){
        this.friendService = friendService;
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponseDto<List<PendingFriendRequestDto>>> getPendingFriendRequests() {
        List<PendingFriendRequestDto> pendingRequests = friendService.getPendingFriendRequests();
        return ResponseEntity.ok(new ApiResponseDto<>(pendingRequests));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponseDto<Set<UserFriendResultDto>>> getUserFriends() {
        Set<UserFriendResultDto> friends = friendService.getUserFriends();
        return ResponseEntity.ok(new ApiResponseDto<>(friends));
    }
}
