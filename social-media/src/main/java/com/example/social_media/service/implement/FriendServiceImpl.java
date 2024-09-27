package com.example.social_media.service.implement;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.social_media.dto.friend.UserFriendResultDto;
import com.example.social_media.model.node.UserNode;
import com.example.social_media.repository.neo4j.UserNodeRepository;
import com.example.social_media.service.AuthService;
import com.example.social_media.service.FriendService;

@Service
public class FriendServiceImpl implements FriendService{

    private final UserNodeRepository userNodeRepository;
    private final AuthService authService;

    public FriendServiceImpl(UserNodeRepository userNodeRepository, AuthService authService){
        this.userNodeRepository = userNodeRepository;
        this.authService = authService;
    }

    @Override
    public List<UserFriendResultDto> getPendingFriendRequests() {
        Long userId = authService.getCurrentUserId();
        return userNodeRepository.findPendingFriendRequests(userId);
    }

    @Override
    public List<UserFriendResultDto> getUserFriends() {
        Long userId = authService.getCurrentUserId();
        return userNodeRepository.findFriendsInfo(userId);
    }
    
    @Override
    public UserFriendResultDto sendFriendRequest(Long userId, Long targetUserId){
        userNodeRepository.sendFriendRequest(userId, targetUserId);
        return getUserFriendResultDto(userId);
    }

    @Override
    public UserFriendResultDto acceptFriendRequest(Long userId, Long targetUserId){
        userNodeRepository.acceptFriendRequest(userId, targetUserId);
        return getUserFriendResultDto(userId);
    }

    private UserFriendResultDto getUserFriendResultDto(Long userId){
        UserNode senderNode = userNodeRepository.findByUserId(userId);
        return new UserFriendResultDto(
            userId,
            senderNode.getName(),
            senderNode.getPhase(),
            senderNode.getOriginSchool() != null ? senderNode.getOriginSchool().getSchoolName() : null,
            senderNode.getExchangeSchool() != null ? senderNode.getExchangeSchool().getSchoolName() : null
        );
    }

    @Override
    public void rejectFriendRequest(Long userId, Long targetUserId){
        userNodeRepository.rejectFriendRequest(userId, targetUserId);
    }
}
