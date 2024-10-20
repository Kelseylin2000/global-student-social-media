package com.example.social_media.repository.mongo;


import com.example.social_media.model.document.Post;

import java.util.List;
import org.springframework.data.domain.Pageable;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByUserId(Long userId);
    List<Post> findByContentContaining(String keyword);
    List<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
}