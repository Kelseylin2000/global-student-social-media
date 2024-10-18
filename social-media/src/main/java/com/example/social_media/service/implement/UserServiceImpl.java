package com.example.social_media.service.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.social_media.dto.user.BasicInfo;
import com.example.social_media.dto.user.CurrentUserProfileDto;
import com.example.social_media.dto.user.DetailInfo;
import com.example.social_media.dto.user.MutualInfo;
import com.example.social_media.dto.user.TargetUserProfileDto;
import com.example.social_media.dto.user.UserProfileUpdateRequestDto;
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

import java.time.Duration;

@Service
public class UserServiceImpl implements UserService{

    private final UserNodeRepository userNodeRepository;
    private final UserRepository userRepository;
    private final InterestNodeRepository interestNodeRepository;
    private final SchoolNodeRepository schoolNodeRepository;
    private final AuthService authService;

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String BASIC_INFO_KEY = "user:basicInfo:";
    private static final String DETAIL_INFO_KEY = "user:detailInfo:";
    private static final String MUTUAL_INFO_KEY = "user:mutualInfo:";

    public UserServiceImpl(UserNodeRepository userNodeRepository, UserRepository userRepository, InterestNodeRepository interestNodeRepository, SchoolNodeRepository schoolNodeRepository, AuthService authService,  RedisTemplate<String, Object> redisTemplate){
        this.userNodeRepository = userNodeRepository;
        this.userRepository = userRepository;
        this.interestNodeRepository = interestNodeRepository;
        this.schoolNodeRepository = schoolNodeRepository;
        this.authService = authService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<TargetUserProfileDto> findUsersFromOrToTheSameSchool() {
    
        Long currentUserId = authService.getCurrentUserId();
        List<Long> commonSchoolUserId = userNodeRepository.findUsersCommonSchool(currentUserId);
        return findTargetUserProfileByUserIds(commonSchoolUserId, currentUserId);
    }

    @Override
    public List<TargetUserProfileDto> searchUsersByName(String keyword){

        Long currentUserId = authService.getCurrentUserId();
        List<Long> userIdsContainKeyword = userNodeRepository.findUsersByNameWithDetails(keyword);
        return findTargetUserProfileByUserIds(userIdsContainKeyword, currentUserId);
    }

    public List<TargetUserProfileDto> findTargetUserProfileByUserIds(List<Long> userIds, Long currentUserId) {
        List<TargetUserProfileDto> targetUserProfileList = new ArrayList<>();

        for (Long userId : userIds) {
            TargetUserProfileDto targetUserProfile = new TargetUserProfileDto();
            targetUserProfile.setUserId(userId);

            String basicInfoKey = BASIC_INFO_KEY + userId;
            String mutualInfoKey = MUTUAL_INFO_KEY + currentUserId + ":" + userId;

            BasicInfo basicInfo = (BasicInfo) redisTemplate.opsForValue().get(basicInfoKey);
            if (basicInfo == null) {
                UserNode userNode = userNodeRepository.findByUserId(userId);
                if (userNode != null) {
                    basicInfo = new BasicInfo(
                        userNode.getName(),
                        userNode.getPhase(),
                        userNode.getOriginSchool() != null ? userNode.getOriginSchool().getSchoolName() : null,
                        userNode.getExchangeSchool() != null ? userNode.getExchangeSchool().getSchoolName() : null,
                        userNode.getExchangeSchool() != null ? userNode.getExchangeSchool().getNationId() : null
                    );
                    redisTemplate.opsForValue().set(basicInfoKey, basicInfo, Duration.ofHours(12));
                }
            }
            if (basicInfo != null) {
                targetUserProfile.setName(basicInfo.getName());
                targetUserProfile.setPhase(basicInfo.getPhase());
                targetUserProfile.setOriginSchoolName(basicInfo.getOriginSchoolName());
                targetUserProfile.setExchangeSchoolName(basicInfo.getExchangeSchoolName());
            }

            MutualInfo mutualInfo = (MutualInfo) redisTemplate.opsForValue().get(mutualInfoKey);
            if (mutualInfo == null) {
                TargetUserProfileDto mutualData = userNodeRepository.findUserProfileWithMutualInfo(currentUserId, userId);
                mutualInfo = new MutualInfo(
                    mutualData.getMutualFriends(),
                    mutualData.getMutualInterests(),
                    mutualData.getRelationship()
                );
                redisTemplate.opsForValue().set(mutualInfoKey, mutualInfo, Duration.ofHours(12));
            }
            targetUserProfile.setMutualFriends(mutualInfo.getMutualFriends());
            targetUserProfile.setMutualInterests(mutualInfo.getMutualInterests());
            targetUserProfile.setRelationship(mutualInfo.getRelationship());

            targetUserProfileList.add(targetUserProfile);
        }

        return targetUserProfileList.stream()
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
    public CurrentUserProfileDto getUserProfileById(){

        Long userId = authService.getCurrentUserId();
        String detailInfoKey = DETAIL_INFO_KEY + userId;

        UserNode userNode = userNodeRepository.findByUserId(userId);
        CurrentUserProfileDto currentUserProfile = new CurrentUserProfileDto();
        
        DetailInfo detailInfo = (DetailInfo) redisTemplate.opsForValue().get(detailInfoKey);
        if (detailInfo == null) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

            List<String> interests = userNodeRepository.findUserInterestsByUserId(userId);

            detailInfo = new DetailInfo(user.getIntroduction(), interests);
            redisTemplate.opsForValue().set(detailInfoKey, detailInfo, Duration.ofHours(12));
        }
        if (detailInfo != null) {
            currentUserProfile.setIntroduction(detailInfo.getIntroduction());
            currentUserProfile.setInterests(detailInfo.getInterests());
        }

        List<String> interestedSchools = userNode.getInterestedSchools().stream()
            .map(SchoolNode::getSchoolName)
            .collect(Collectors.toList());

        currentUserProfile.setUserId(userId);
        currentUserProfile.setName(userNode.getName());
        currentUserProfile.setPhase(userNode.getPhase() != null ? userNode.getPhase().toString() : null);
        currentUserProfile.setOriginSchoolName(userNode.getOriginSchool() != null ? userNode.getOriginSchool().getSchoolName() : null);
        currentUserProfile.setExchangeSchoolName(userNode.getExchangeSchool() != null ? userNode.getExchangeSchool().getSchoolName() : null);
        currentUserProfile.setInterestedSchools(interestedSchools);

        return currentUserProfile;
    }
    

    @Override
    public TargetUserProfileDto getUserProfileById(Long userId) {
        Long currentUserId = authService.getCurrentUserId();

        String basicInfoKey = BASIC_INFO_KEY + userId;
        String detailInfoKey = DETAIL_INFO_KEY + userId;
        String mutualInfoKey = MUTUAL_INFO_KEY + currentUserId + ":" + userId;

        TargetUserProfileDto targetUserProfile = new TargetUserProfileDto();
        targetUserProfile.setUserId(userId);

        BasicInfo basicInfo = (BasicInfo) redisTemplate.opsForValue().get(basicInfoKey);
        if (basicInfo == null) {
            UserNode userNode = userNodeRepository.findByUserId(userId);
            if (userNode != null) {
                basicInfo = new BasicInfo(
                    userNode.getName(),
                    userNode.getPhase(),
                    userNode.getOriginSchool() != null ? userNode.getOriginSchool().getSchoolName() : null,
                    userNode.getExchangeSchool() != null ? userNode.getExchangeSchool().getSchoolName() : null,
                    userNode.getExchangeSchool() != null ? userNode.getExchangeSchool().getNationId() : null
                );
                redisTemplate.opsForValue().set(basicInfoKey, basicInfo, Duration.ofHours(12));
            }
        }
        if (basicInfo != null) {
            targetUserProfile.setName(basicInfo.getName());
            targetUserProfile.setPhase(basicInfo.getPhase());
            targetUserProfile.setOriginSchoolName(basicInfo.getOriginSchoolName());
            targetUserProfile.setExchangeSchoolName(basicInfo.getExchangeSchoolName());
        }

        DetailInfo detailInfo = (DetailInfo) redisTemplate.opsForValue().get(detailInfoKey);
        if (detailInfo == null) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

            List<String> interests = userNodeRepository.findUserInterestsByUserId(userId);

            detailInfo = new DetailInfo(user.getIntroduction(), interests);
            redisTemplate.opsForValue().set(detailInfoKey, detailInfo, Duration.ofHours(12));
        }
        if (detailInfo != null) {
            targetUserProfile.setIntroduction(detailInfo.getIntroduction());
            targetUserProfile.setInterests(detailInfo.getInterests());
        }

        MutualInfo mutualInfo = (MutualInfo) redisTemplate.opsForValue().get(mutualInfoKey);
        if (mutualInfo == null) {
            TargetUserProfileDto mutualData = userNodeRepository.findUserProfileWithMutualInfo(currentUserId, userId);
            mutualInfo = new MutualInfo(
                mutualData.getMutualFriends(),
                mutualData.getMutualInterests(),
                mutualData.getRelationship()
            );
            redisTemplate.opsForValue().set(mutualInfoKey, mutualInfo, Duration.ofHours(12));
        }
        if (mutualInfo != null) {
            targetUserProfile.setMutualFriends(mutualInfo.getMutualFriends());
            targetUserProfile.setMutualInterests(mutualInfo.getMutualInterests());
            targetUserProfile.setRelationship(mutualInfo.getRelationship());
        }

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
            String basicInfoKey = BASIC_INFO_KEY + userId;
            redisTemplate.delete(basicInfoKey);
        }
        if (introduction != null) {
            user.setIntroduction(introduction);
            String detailInfoKey = DETAIL_INFO_KEY + userId;
            redisTemplate.delete(detailInfoKey);
        }
        userRepository.save(user);
    }

    @Transactional("neo4jTransactionManager")
    private void updateUserProfileInNeo4j(Long userId, String name, List<Integer> interests) {
        UserNode userNode = userNodeRepository.findByUserId(userId);
        if (userNode != null) {
            if (name != null) {
                userNode.setName(name);
                String basicInfoKey = BASIC_INFO_KEY + userId;
                redisTemplate.delete(basicInfoKey);
            }
            if (interests != null) {
                List<InterestNode> interestNodes = interestNodeRepository.findByInterestIdIn(interests);
                userNode.setInterests(interestNodes);
                String detailInfoKey = DETAIL_INFO_KEY + userId;
                redisTemplate.delete(detailInfoKey);

                String mutualInfoPatternStart = MUTUAL_INFO_KEY + userId + ":*";
                Set<String> mutualInfoKeysStart = redisTemplate.keys(mutualInfoPatternStart);
                if (mutualInfoKeysStart != null && !mutualInfoKeysStart.isEmpty()) {
                    redisTemplate.delete(mutualInfoKeysStart);
                }

                String mutualInfoPatternEnd = MUTUAL_INFO_KEY + "*:" + userId;
                Set<String> mutualInfoKeysEnd = redisTemplate.keys(mutualInfoPatternEnd);
                if (mutualInfoKeysEnd != null && !mutualInfoKeysEnd.isEmpty()) {
                    redisTemplate.delete(mutualInfoKeysEnd);
                }
            }
            userNodeRepository.save(userNode);
        }
    }

    @Override
    public void updateInterestedSchools(List<Integer> schoolIds) {
        Long userId = authService.getCurrentUserId();

        UserNode userNode = userNodeRepository.findByUserId(userId);
        List<SchoolNode> schoolNodes = schoolNodeRepository.findBySchoolIdIn(schoolIds);
        userNode.setInterestedSchools(schoolNodes);

        userNodeRepository.save(userNode);
    }

    @Override
    public void deleteAllInterestedSchools() {
        Long userId = authService.getCurrentUserId();
        UserNode user = userNodeRepository.findByUserId(userId);

        if (user != null) {
            user.setInterestedSchools(new ArrayList<>());
            userNodeRepository.save(user);
        }
    }

    @Override
    public void updateUserPhase(String phase) {
        Long userId = authService.getCurrentUserId();
        UserNode userNode = userNodeRepository.findByUserId(userId);
        userNode.setPhase(phase);
        userNodeRepository.save(userNode);

        String basicInfoKey = BASIC_INFO_KEY + userId;
        redisTemplate.delete(basicInfoKey);
    }

    @Override
    public void updateUserExchangeSchool(Long schoolId) {
        Long userId = authService.getCurrentUserId();
        SchoolNode exchangeSchool = schoolNodeRepository.findBySchoolId(schoolId);
        UserNode userNode = userNodeRepository.findByUserId(userId);
        userNode.setExchangeSchool(exchangeSchool);
        userNodeRepository.save(userNode);

        String basicInfoKey = BASIC_INFO_KEY + userId;
        redisTemplate.delete(basicInfoKey);
    }

    @Override
    public void deleteUserExchangeSchool() {
        Long userId = authService.getCurrentUserId();
        UserNode userNode = userNodeRepository.findByUserId(userId);

        if (userNode != null) {
            userNode.setExchangeSchool(null);
            userNodeRepository.save(userNode);

            String basicInfoKey = BASIC_INFO_KEY + userId;
            redisTemplate.delete(basicInfoKey);
        }
    }

    @Override
    public void updateUserOriginSchool(Long schoolId) {
        Long userId = authService.getCurrentUserId();
        SchoolNode originSchool = schoolNodeRepository.findBySchoolId(schoolId);
        UserNode userNode = userNodeRepository.findByUserId(userId);
        userNode.setOriginSchool(originSchool);
        userNodeRepository.save(userNode);

        String basicInfoKey = BASIC_INFO_KEY + userId;
        redisTemplate.delete(basicInfoKey);
    }
}