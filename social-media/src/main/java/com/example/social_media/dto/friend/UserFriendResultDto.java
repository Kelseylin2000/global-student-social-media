package com.example.social_media.dto.friend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFriendResultDto {

    private Long userId;
    private String name;
    private String phase;
    private Long originSchoolId;
    private String originSchoolName;
    private Long exchangeSchoolId;
    private String exchangeSchoolName;
}
