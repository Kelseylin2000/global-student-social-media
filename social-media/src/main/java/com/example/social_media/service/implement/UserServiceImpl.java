package com.example.social_media.service.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.social_media.dto.user.CurrentUserProfileDto;
import com.example.social_media.dto.user.TargetUserProfileDto;
import com.example.social_media.dto.user.UserNodeWithMutualInfoDto;
import com.example.social_media.dto.user.UserProfileUpdateRequestDto;
import com.example.social_media.dto.user.UserSearchResultDto;
import com.example.social_media.model.entity.User;
import com.example.social_media.model.node.InterestNode;
import com.example.social_media.model.node.SchoolNode;
import com.example.social_media.model.node.UserNode;
import com.example.social_media.repository.mysql.UserRepository;
import com.example.social_media.repository.neo4j.InterestNodeRepository;
import com.example.social_media.repository.neo4j.SchoolNodeRepository;
import com.example.social_media.repository.neo4j.UserNodeRepository;
import com.example.social_media.service.AuthService;
import com.example.social_media.service.UserService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserServiceImpl implements UserService{

    private final UserNodeRepository userNodeRepository;
    private final UserRepository userRepository;
    private final InterestNodeRepository interestNodeRepository;
    private final SchoolNodeRepository schoolNodeRepository;
    private final AuthService authService;

    public UserServiceImpl(UserNodeRepository userNodeRepository, UserRepository userRepository, InterestNodeRepository interestNodeRepository, SchoolNodeRepository schoolNodeRepository, AuthService authService){
        this.userNodeRepository = userNodeRepository;
        this.userRepository = userRepository;
        this.interestNodeRepository = interestNodeRepository;
        this.schoolNodeRepository = schoolNodeRepository;
        this.authService = authService;
    }

    @Override
    public List<UserSearchResultDto> findUsersFromOrToTheSameSchool(){

        Long userId = authService.getCurrentUserId();

        List<UserSearchResultDto> fromSpecificSchool = convertToDto(userNodeRepository.findUsersFromSpecificSchool(userId));
        List<UserSearchResultDto> toSpecificSchool = convertToDto(userNodeRepository.findUsersToSpecificSchool(userId));
        
        List<UserSearchResultDto> combinedList = new ArrayList<>();
        combinedList.addAll(fromSpecificSchool);
        combinedList.addAll(toSpecificSchool);

        return combinedList.stream()
            .sorted((p1, p2) -> {
                int friendCountComparison = Integer.compare(p2.getMutualFriends().size(), p1.getMutualFriends().size());
                if (friendCountComparison != 0) {
                    return friendCountComparison;
                } else {
                    return Integer.compare(p2.getMutualInterests().size(), p1.getMutualInterests().size());
                }
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<UserSearchResultDto> searchUsersByName(String keyword){
        List<UserSearchResultDto> usersContainKeyword = convertToDto(userNodeRepository.findUsersByNameWithDetails(keyword));
        
        return usersContainKeyword.stream()
            .sorted((p1, p2) -> {
                int friendCountComparison = Integer.compare(p2.getMutualFriends().size(), p1.getMutualFriends().size());
                if (friendCountComparison != 0) {
                    return friendCountComparison;
                } else {
                    return Integer.compare(p2.getMutualInterests().size(), p1.getMutualInterests().size());
                }
            })
            .collect(Collectors.toList());
    }

    private List<UserSearchResultDto> convertToDto(List<UserNodeWithMutualInfoDto> users) {
    return users.stream()
            .map(user -> {
                UserNode userNode = user.getTarget(); 
                return new UserSearchResultDto(
                    userNode.getUserId(),
                    userNode.getName(),
                    userNode.getPhase() != null ? userNode.getPhase().toString() : null,
                    userNode.getOriginSchool() != null ? userNode.getOriginSchool().getSchoolId() : null,
                    userNode.getOriginSchool() != null ? userNode.getOriginSchool().getSchoolName() : null,
                    userNode.getExchangeSchool() != null ? userNode.getExchangeSchool().getSchoolId() : null,
                    userNode.getExchangeSchool() != null ? userNode.getExchangeSchool().getSchoolName() : null,
                    user.getMutualFriends(),
                    user.getMutualInterests()
                );
            })
            .collect(Collectors.toList());
    }

    @Override
    public CurrentUserProfileDto getUserProfileById(){

        Long userId = authService.getCurrentUserId();
        UserNode userNode = userNodeRepository.findByUserId(userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        List<String> interests = userNode.getInterests().stream()
            .map(InterestNode::getInterest)
            .collect(Collectors.toList());

        List<String> interestedSchools = userNode.getInterestedSchools().stream()
            .map(SchoolNode::getSchoolName)
            .collect(Collectors.toList());
        
        CurrentUserProfileDto currentUserProfile = new CurrentUserProfileDto(
            userId,
            userNode.getName(),
            userNode.getPhase() != null ? userNode.getPhase().toString() : null,
            user.getIntroduction(),
            user.getFromSchoolEmail(),
            user.getToSchoolEmail(),
            userNode.getOriginSchool() != null ? userNode.getOriginSchool().getSchoolId() : null,
            userNode.getOriginSchool() != null ? userNode.getOriginSchool().getSchoolName() : null,
            userNode.getExchangeSchool() != null ? userNode.getExchangeSchool().getSchoolId() : null,
            userNode.getExchangeSchool() != null ? userNode.getExchangeSchool().getSchoolName() : null,
            interests,
            interestedSchools
        );

        return currentUserProfile;
    }

    @Override
    public TargetUserProfileDto getUserProfileById(Long userId){
        Long currentUserId = authService.getCurrentUserId();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        TargetUserProfileDto targetUserProfile = userNodeRepository.findUserProfileWithMutualInfo(currentUserId, userId);
        targetUserProfile.setIntroduction(user.getIntroduction());
        targetUserProfile.setInterests(userNodeRepository.findUserInterestsByUserId(userId));
        UserNode userNode = userNodeRepository.findByUserId(userId);
        targetUserProfile.setPhase(userNode.getPhase() != null ? userNode.getPhase() : null);
        targetUserProfile.setOriginSchoolName(userNode.getOriginSchool() != null ? userNode.getOriginSchool().getSchoolName() : null);
        targetUserProfile.setExchangeSchoolName(userNode.getExchangeSchool() != null ? userNode.getExchangeSchool().getSchoolName() : null);

        return targetUserProfile;
    }

    @Override
    public void updateUserProfile(UserProfileUpdateRequestDto profileUpdateRequest) {
        Long userId = authService.getCurrentUserId();
        
        updateUserProfileInMySQL(userId, profileUpdateRequest.getName(), profileUpdateRequest.getIntroduction());
        updateUserProfileInNeo4j(userId, profileUpdateRequest.getName(), profileUpdateRequest.getInterests());
    }

    @Transactional("mysqlTransactionManager")
    private void updateUserProfileInMySQL(Long userId, String name, String introduction) {
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (name != null) {
            user.setName(name);
        }
        if(introduction != null){
            user.setIntroduction(introduction);
        }
        userRepository.save(user);
    }

    @Transactional("neo4jTransactionManager")
    private void updateUserProfileInNeo4j(Long userId, String name, List<Integer> interests) {
        UserNode userNode = userNodeRepository.findByUserId(userId);
        if (userNode != null) {
            if (name != null) {
                userNode.setName(name);
            }
            if (interests != null) {
                List<InterestNode> interestNodes = interestNodeRepository.findByInterestIdIn(interests);
                userNode.setInterests(interestNodes);
            }
            userNodeRepository.save(userNode);
        }
    }

    @Override
    public void updateInterestedSchools(List<Integer> schoolIds){
        Long userId = authService.getCurrentUserId();

        UserNode userNode = userNodeRepository.findByUserId(userId);
        List<SchoolNode> schoolNodes = schoolNodeRepository.findBySchoolIdIn(schoolIds);
        userNode.setInterestedSchools(schoolNodes);

        userNodeRepository.save(userNode);
    }
}
