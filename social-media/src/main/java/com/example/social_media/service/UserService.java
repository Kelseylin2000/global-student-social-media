package com.example.social_media.service;

import java.util.List;

import com.example.social_media.dto.user.CurrentUserProfileDto;
import com.example.social_media.dto.user.TargetUserProfileDto;
import com.example.social_media.dto.user.UserProfileUpdateRequestDto;

public interface UserService {
    List<TargetUserProfileDto> findUsersFromOrToTheSameSchool();
    List<TargetUserProfileDto> searchUsersByName(String keyword);
    
    CurrentUserProfileDto getUserProfileById();
    TargetUserProfileDto getUserProfileById(Long userId);

    void updateUserProfile(UserProfileUpdateRequestDto profileUpdateRequest);
    void updateInterestedSchools(List<Integer> schoolIds);
    void deleteAllInterestedSchools();
    
    void updateUserPhase(String phase);
    void updateUserExchangeSchool(Long schoolId);
    void deleteUserExchangeSchool();
    void updateUserOriginSchool(Long schoolId);
}
