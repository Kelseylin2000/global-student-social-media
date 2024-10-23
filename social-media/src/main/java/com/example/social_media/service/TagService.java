package com.example.social_media.service;

import java.util.List;
import java.util.Map;

public interface TagService {
    void updateUserTagCount(Long userId, List<String> tags);
    Map<String, Integer> getUserTagCounts(Long userId);
}