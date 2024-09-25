package com.example.social_media.dto.user;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetUserProfileDto {

    private Long userId;
    private String name;
    private String phase;
    private String introduction;

    private String originSchoolName;
    private String exchangeSchoolName;
    private List<String> interests;

    private List<String> mutualFriends;
    private List<String> mutualInterests;
    
    private String relationship;
}
