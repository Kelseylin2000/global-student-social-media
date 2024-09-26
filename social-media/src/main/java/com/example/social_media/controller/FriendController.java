package com.example.social_media.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.social_media.dto.ApiResponseDto;
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
    public ResponseEntity<ApiResponseDto<List<UserFriendResultDto>>> getPendingFriendRequests() {
        List<UserFriendResultDto> pendingRequests = friendService.getPendingFriendRequests();
        return ResponseEntity.ok(new ApiResponseDto<>(pendingRequests));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponseDto<List<UserFriendResultDto>>> getUserFriends() {
        List<UserFriendResultDto> friends = friendService.getUserFriends();
        return ResponseEntity.ok(new ApiResponseDto<>(friends));
    }
}
