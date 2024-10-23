package com.example.social_media.service.implement;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.social_media.service.TagService;

@Service
public class TagServiceImpl implements TagService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${tag.count.days.range:5}")
    private int daysRange;

    private static final String USER_TAGS_KEY = "user:tags:";

    public TagServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void updateUserTagCount(Long userId, List<String> tags) {
        String today = LocalDate.now().toString();
        String redisKey = USER_TAGS_KEY + userId + ":" + today;

        tags.forEach(tag -> redisTemplate.opsForHash().increment(redisKey, tag, 1));
        redisTemplate.expire(redisKey, daysRange, TimeUnit.DAYS);
    }

    @Override
    public Map<String, Integer> getUserTagCounts(Long userId) {
        Map<String, Integer> tagCounts = new HashMap<>();
        List<String> redisKeys = new ArrayList<>();
        
        for (int i = 0; i < daysRange; i++) {
            String date = LocalDate.now().minusDays(i).toString();
            String redisKey = USER_TAGS_KEY + userId + ":" + date;
            redisKeys.add(redisKey);
        }

        for (String redisKey : redisKeys) {
            Map<Object, Object> dailyTags = redisTemplate.opsForHash().entries(redisKey);

            int weight = daysRange - redisKeys.indexOf(redisKey);
            for (Map.Entry<Object, Object> entry : dailyTags.entrySet()) {
                String tag = (String) entry.getKey();
                int count = Integer.parseInt(entry.getValue().toString());
                tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + count * weight);
            }
        }

        return tagCounts;
    }
}
