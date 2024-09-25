package com.example.social_media.service.implement;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.social_media.dto.friend.PendingFriendRequestDto;
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
    public List<PendingFriendRequestDto> getPendingFriendRequests() {

        Long userId = authService.getCurrentUserId();
        
        return userNodeRepository.findPendingFriendRequests(userId);
    }

    @Override
    public List<UserFriendResultDto> getUserFriends() {
        
        Long userId = authService.getCurrentUserId();
        UserNode user = userNodeRepository.findByUserId(userId);

        return user != null ? convertToDto(user.getFriends()) : List.of();
    }

    private List<UserFriendResultDto> convertToDto(List<UserNode> friends) {

    return friends.stream()
            .map(friend -> new UserFriendResultDto(
                    friend.getUserId(),
                    friend.getName(),
                    friend.getPhase() != null ? friend.getPhase().toString() : null,
                    friend.getOriginSchool() != null ? friend.getOriginSchool().getSchoolId() : null,
                    friend.getOriginSchool() != null ? friend.getOriginSchool().getSchoolName() : null,
                    friend.getExchangeSchool() != null ? friend.getExchangeSchool().getSchoolId() : null,
                    friend.getExchangeSchool() != null ? friend.getExchangeSchool().getSchoolName() : null
            ))
            .collect(Collectors.toList());
    }

    @Override
    public void sendFriendRequest(Long userId, Long targetUserId){
        userNodeRepository.sendFriendRequest(userId, targetUserId);
    }

    @Override
    public void acceptFriendRequest(Long userId, Long targetUserId){
        userNodeRepository.acceptFriendRequest(userId, targetUserId);
    }

    @Override
    public void rejectFriendRequest(Long userId, Long targetUserId){
        userNodeRepository.rejectFriendRequest(userId, targetUserId);
    }
}
