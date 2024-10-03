package com.example.social_media.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.social_media.dto.ApiResponseDto;
import com.example.social_media.dto.user.CurrentUserProfileDto;
import com.example.social_media.dto.user.TargetUserProfileDto;
import com.example.social_media.dto.user.UserProfileUpdateRequestDto;
import com.example.social_media.service.UserService;

import java.util.List;

import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/api/1.0/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/same-school")
    public ResponseEntity<ApiResponseDto<List<TargetUserProfileDto>>> findUsersFromOrToTheSameSchool() {
        List<TargetUserProfileDto> potentialFriends = userService.findUsersFromOrToTheSameSchool();
        return ResponseEntity.ok(new ApiResponseDto<>(potentialFriends));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<TargetUserProfileDto>>> searchUsersByName(@RequestParam String keyword) {
        List<TargetUserProfileDto> users = userService.searchUsersByName(keyword);
        return ResponseEntity.ok(new ApiResponseDto<>(users));
    }

    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponseDto<CurrentUserProfileDto>> getMyProfile() {
        CurrentUserProfileDto myProfile = userService.getUserProfileById();
        return ResponseEntity.ok(new ApiResponseDto<>(myProfile));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponseDto<TargetUserProfileDto>> getUserProfile(@PathVariable Long userId) {
        TargetUserProfileDto userProfile = userService.getUserProfileById(userId);
        return ResponseEntity.ok(new ApiResponseDto<>(userProfile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponseDto<String>> updateUserProfile(@RequestBody UserProfileUpdateRequestDto profileUpdateRequest) {
        userService.updateUserProfile(profileUpdateRequest);
        return ResponseEntity.ok(new ApiResponseDto<>("User's profile updated successfully"));
    }

    @PutMapping("/interested-schools")
    public ResponseEntity<ApiResponseDto<String>> updateInterestedSchools(@RequestBody List<Integer> schoolIds) {
        userService.updateInterestedSchools(schoolIds);
        return ResponseEntity.ok(new ApiResponseDto<>("User's interested schools updated successfully"));
    }

    @DeleteMapping("/interested-schools")
    public ResponseEntity<ApiResponseDto<String>> deleteAllInterestedSchools() {
        userService.deleteAllInterestedSchools();
        return ResponseEntity.ok(new ApiResponseDto<>("User's all interested schools deleted successfully"));
    }

    @PutMapping("/me/phase")
    public ResponseEntity<ApiResponseDto<String>> updateUserPhase(@RequestBody String phase) {
        userService.updateUserPhase(phase);
        return ResponseEntity.ok(new ApiResponseDto<>("User's phase updated successfully"));
    }

    @PutMapping("/me/exchange-school")
    public ResponseEntity<ApiResponseDto<String>> updateExchangeSchool(@RequestBody Long schoolId) {
        userService.updateUserExchangeSchool(schoolId);
        return ResponseEntity.ok(new ApiResponseDto<>("User's exchange school updated successfully"));
    }

    @PutMapping("/me/origin-school")
    public ResponseEntity<ApiResponseDto<String>> updateOriginSchool(@RequestBody Long schoolId) {
        userService.updateUserOriginSchool(schoolId);
        return ResponseEntity.ok(new ApiResponseDto<>("User's origin school updated successfully"));
    }
}