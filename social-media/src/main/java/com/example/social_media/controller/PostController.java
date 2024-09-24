package com.example.social_media.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.social_media.dto.post.CommentDto;
import com.example.social_media.dto.post.PostRequestDto;
import com.example.social_media.dto.post.PostWithCommentDto;
import com.example.social_media.dto.post.PostWithoutCommentsDto;
import com.example.social_media.service.PostService;
import com.example.social_media.dto.ApiResponseDto;

import java.util.List;

import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/1.0/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<PostWithoutCommentsDto>> createPost(@ModelAttribute PostRequestDto postRequest, @RequestParam(value = "imagesFiles", required = false) MultipartFile[] imagesFiles) {
        PostWithoutCommentsDto createdPost = postService.createPost(postRequest, imagesFiles);
        return ResponseEntity.ok(new ApiResponseDto<>(createdPost));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponseDto<PostWithoutCommentsDto>> updatePost(@PathVariable String postId, @ModelAttribute PostRequestDto postRequest, @RequestParam(value = "imagesFiles", required = false) MultipartFile[] imagesFiles) {
        PostWithoutCommentsDto updatedPost = postService.updatePost(postId, postRequest, imagesFiles);
        return ResponseEntity.ok(new ApiResponseDto<>(updatedPost));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponseDto<String>> deletePost(@PathVariable String postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok(new ApiResponseDto<>("Post deleted successfully"));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponseDto<PostWithCommentDto>> getPostById(@PathVariable String postId) {
        PostWithCommentDto post = postService.getPostWithCommentById(postId);
        return ResponseEntity.ok(new ApiResponseDto<>(post));
    }

    @PostMapping("/{postId}/comment")
    public ResponseEntity<ApiResponseDto<CommentDto>> addComment(
            @PathVariable String postId, 
            @RequestBody CommentDto commentDto) {
    
        CommentDto comment = postService.addComment(postId, commentDto);
        return ResponseEntity.ok(new ApiResponseDto<>(comment));
    }

    @DeleteMapping("/{postId}/comment/{commentId}")
    public ResponseEntity<ApiResponseDto<String>> deleteComment(
            @PathVariable String postId, 
            @PathVariable String commentId) {
        
        postService.deleteComment(commentId);
        return ResponseEntity.ok(new ApiResponseDto<>("Comment deleted successfully"));
    }

    @PostMapping("/{postId}/save")
    public ResponseEntity<ApiResponseDto<String>> savePost(@PathVariable String postId) {
        postService.savePost(postId);
        return ResponseEntity.ok(new ApiResponseDto<>("Post saved successfully"));
    }

    @DeleteMapping("/{postId}/save")
    public ResponseEntity<ApiResponseDto<String>> unsavePost(@PathVariable String postId) {
        postService.unsavePost(postId);
        return ResponseEntity.ok(new ApiResponseDto<>("Post unsaved successfully"));
    }

    @GetMapping("/saved")
    public ResponseEntity<ApiResponseDto<List<PostWithoutCommentsDto>>> getSavedPosts() {
        List<PostWithoutCommentsDto> savedPosts = postService.getSavedPosts();
        return ResponseEntity.ok(new ApiResponseDto<>(savedPosts));
    }

    @PostMapping("/{postId}/interactions")
    public ResponseEntity<ApiResponseDto<String>> recordPostInteraction(@PathVariable String postId) {

        postService.incrementPostViews(postId);
        postService.logBrowsingHistory(postId);
        postService.updateUserTagCount(postId);

        return ResponseEntity.ok(new ApiResponseDto<>("Interaction recorded successfully"));
    }

    @GetMapping("/recommended")
    public ResponseEntity<Page<PostWithoutCommentsDto>> getRecommendedPosts(
            @RequestParam(defaultValue = "0") int page, 
            @RequestParam(defaultValue = "20") int size) {

        Page<PostWithoutCommentsDto> recommendedPosts = postService.getRecommendedPosts(page, size);
        return new ResponseEntity<>(recommendedPosts, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseDto<List<PostWithoutCommentsDto>>> getPostsByUserId(
            @PathVariable Long userId) {
        List<PostWithoutCommentsDto> userPosts = postService.getPostsByUserId(userId);
        return ResponseEntity.ok(new ApiResponseDto<>(userPosts));
    }
}
