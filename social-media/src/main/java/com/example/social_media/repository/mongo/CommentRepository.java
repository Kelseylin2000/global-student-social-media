package com.example.social_media.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.social_media.model.document.Comment;
import java.util.List;


public interface CommentRepository extends MongoRepository<Comment, String>{
    Long countByPostId(String postId);
    List<Comment> findByPostId(String postId);
}
