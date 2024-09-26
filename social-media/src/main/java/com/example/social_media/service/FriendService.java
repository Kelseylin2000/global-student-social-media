package com.example.social_media.service;

import java.util.List;

import com.example.social_media.dto.friend.UserFriendResultDto;

public interface FriendService {

    List<UserFriendResultDto> getPendingFriendRequests();
    List<UserFriendResultDto> getUserFriends();

    UserFriendResultDto sendFriendRequest(Long userId, Long targetUserId);
    void acceptFriendRequest(Long userId, Long targetUserId);
    void rejectFriendRequest(Long userId, Long targetUserId);
}
