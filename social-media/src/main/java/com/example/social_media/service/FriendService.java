package com.example.social_media.service;

import java.util.List;
import java.util.Set;

import com.example.social_media.dto.friend.PendingFriendRequestDto;
import com.example.social_media.dto.friend.UserFriendResultDto;

public interface FriendService {

    List<PendingFriendRequestDto> getPendingFriendRequests();
    Set<UserFriendResultDto> getUserFriends();

    void sendFriendRequest(Long userId, Long targetUserId);
    void acceptFriendRequest(Long userId, Long targetUserId);
    void rejectFriendRequest(Long userId, Long targetUserId);
}
