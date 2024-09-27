package com.example.social_media.service;

import java.util.List;

import com.example.social_media.dto.user.CurrentUserProfileDto;
import com.example.social_media.dto.user.TargetUserProfileDto;
import com.example.social_media.dto.user.UserProfileUpdateRequestDto;
import com.example.social_media.dto.user.UserSearchResultDto;

public interface UserService {
    List<TargetUserProfileDto> findUsersFromOrToTheSameSchool();
    List<UserSearchResultDto> searchUsersByName(String keyword);
    
    CurrentUserProfileDto getUserProfileById();
    TargetUserProfileDto getUserProfileById(Long userId);

    void updateUserProfile(UserProfileUpdateRequestDto profileUpdateRequest);
    void updateInterestedSchools(List<Integer> schoolIds);
}
