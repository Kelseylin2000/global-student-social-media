package com.example.social_media.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrowsingHistoryDto implements Serializable{
    private Long userId;
    private String postId;
    String timestampStr;
}
