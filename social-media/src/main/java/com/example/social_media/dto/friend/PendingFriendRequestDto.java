package com.example.social_media.dto.friend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingFriendRequestDto {

    private Long senderId;
    private String senderName;
}