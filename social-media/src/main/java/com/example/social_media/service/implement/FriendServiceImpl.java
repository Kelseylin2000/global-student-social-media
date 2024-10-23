package com.example.social_media.service.implement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String MUTUAL_INFO_KEY = "user:mutualInfo:";

    public FriendServiceImpl(UserNodeRepository userNodeRepository, AuthService authService, RedisTemplate<String, Object> redisTemplate){
        this.userNodeRepository = userNodeRepository;
        this.authService = authService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<UserFriendResultDto> getPendingFriendRequests() {
        Long currentUserId = authService.getCurrentUserId();
        return userNodeRepository.findPendingFriendRequests(currentUserId);
    }

    @Override
    public List<UserFriendResultDto> getUserFriends() {
        Long currentUserId = authService.getCurrentUserId();
        return userNodeRepository.findFriendsInfo(currentUserId);
    }
    
    @Override
    public UserFriendResultDto sendFriendRequest(Long currentUserId, Long targetUserId){
        userNodeRepository.sendFriendRequest(currentUserId, targetUserId);
        clearMutualInfoCache(currentUserId, targetUserId);
        return getUserFriendResultDto(currentUserId);
    }

    @Override
    public UserFriendResultDto acceptFriendRequest(Long currentUserId, Long targetUserId){
        userNodeRepository.acceptFriendRequest(currentUserId, targetUserId);
        clearMutualInfoCache(currentUserId, targetUserId);
        return getUserFriendResultDto(currentUserId);
    }

    private void clearMutualInfoCache(Long currentUserId, Long targetUserId) {
        String mutualInfoKey1 = MUTUAL_INFO_KEY + currentUserId + ":" + targetUserId;
        redisTemplate.delete(mutualInfoKey1);
        String mutualInfoKey2 = MUTUAL_INFO_KEY + targetUserId + ":" + currentUserId;
        redisTemplate.delete(mutualInfoKey2);
    }

    private UserFriendResultDto getUserFriendResultDto(Long currentUserId){
        UserNode senderNode = userNodeRepository.findByUserId(currentUserId);
        return new UserFriendResultDto(
            currentUserId,
            senderNode.getName(),
            senderNode.getPhase(),
            Optional.ofNullable(senderNode.getOriginSchool()).map(school -> school.getSchoolName()).orElse(null),
            Optional.ofNullable(senderNode.getExchangeSchool()).map(school -> school.getSchoolName()).orElse(null)
        );
    }

    @Override
    public void rejectFriendRequest(Long currentUserId, Long targetUserId){
        userNodeRepository.rejectFriendRequest(currentUserId, targetUserId);
        clearMutualInfoCache(currentUserId, targetUserId);
    }
}