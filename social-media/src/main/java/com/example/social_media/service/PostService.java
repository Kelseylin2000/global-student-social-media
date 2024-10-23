package com.example.social_media.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.example.social_media.dto.post.CommentDto;
import com.example.social_media.dto.post.PostRequestDto;
import com.example.social_media.dto.post.PostWithCommentDto;
import com.example.social_media.dto.post.PostWithoutCommentsDto;

public interface PostService {

    PostWithoutCommentsDto createPost(PostRequestDto postRequest, MultipartFile[] imagesFiles);
    PostWithoutCommentsDto updatePost(String postId, PostRequestDto postRequest, MultipartFile[] imagesFiles);
    void deletePost(String postId);

    Page<PostWithoutCommentsDto> getRecommendedPosts(int page, int size);
    List<PostWithoutCommentsDto> getPostsByUserId(long userId);
    List<PostWithoutCommentsDto> getPostsByKeyword(String keyword);
    PostWithCommentDto getPostWithCommentById(String postId);
    
    CommentDto addComment(String postId, CommentDto commentDto);
    void deleteComment(String commentId);

    void savePost(String postId);
    void unsavePost(String postId);
    List<PostWithoutCommentsDto> getSavedPosts();

    void recordBrowsingHistory(String postId);

    void batchUpdatePostViews();
    void batchSaveBrowsingHistory();
}
