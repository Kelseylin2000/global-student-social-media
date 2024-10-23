package com.example.social_media.service.implement;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.example.social_media.dto.post.BrowsingHistoryDto;
import com.example.social_media.dto.post.CommentDto;
import com.example.social_media.dto.post.PostRequestDto;
import com.example.social_media.dto.post.PostWithCommentDto;
import com.example.social_media.dto.post.PostWithoutCommentsDto;
import com.example.social_media.dto.post.ScoredPostDto;
import com.example.social_media.dto.user.BasicInfo;
import com.example.social_media.model.document.BrowsingHistory;
import com.example.social_media.model.document.Comment;
import com.example.social_media.model.document.Post;
import com.example.social_media.model.document.SiteUser;
import com.example.social_media.model.node.SchoolNode;
import com.example.social_media.model.node.UserNode;
import com.example.social_media.repository.mongo.BrowsingHistoryRepository;
import com.example.social_media.repository.mongo.CommentRepository;
import com.example.social_media.repository.mongo.PostRepository;
import com.example.social_media.repository.mongo.SiteUserRepository;
import com.example.social_media.repository.neo4j.UserNodeRepository;
import com.example.social_media.service.AuthService;
import com.example.social_media.service.ImageService;
import com.example.social_media.service.PostService;
import com.example.social_media.service.TagService;

import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Collections;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.Duration;

@Service
public class PostServiceImpl implements PostService{

    private final AuthService authService;
    private final ImageService imageService;
    private final TagService tagService;

    private final PostRepository postRepository;
    private final UserNodeRepository userNodeRepository;
    private final CommentRepository commentRepository;
    private final SiteUserRepository siteUserRepository;
    private final BrowsingHistoryRepository browsingHistoryRepository;
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String POST_VIEWS_KEY = "post:views:";
    private static final String BROWSING_HISTORY_KEY = "user:browsing:history:";
    private static final String BASIC_INFO_KEY = "user:basicInfo:";

    private final MongoTemplate mongoTemplate;

    private static final double lambda = 0.000001;
    private static final double highSchoolScore = 70;
    private static final double mediumCountryScore = 30;
    private static final double tagWeight = 0.5;

    public PostServiceImpl(AuthService authService, ImageService imageService, TagService tagService, PostRepository postRepository, UserNodeRepository userNodeRepository, CommentRepository commentRepository, SiteUserRepository siteUserRepository, BrowsingHistoryRepository browsingHistoryRepository, RedisTemplate<String, Object> redisTemplate, MongoTemplate mongoTemplate){
        this.authService = authService;
        this.imageService = imageService;
        this.tagService = tagService;
        this.postRepository = postRepository;
        this.userNodeRepository = userNodeRepository;
        this.commentRepository = commentRepository;
        this.siteUserRepository = siteUserRepository;
        this.browsingHistoryRepository = browsingHistoryRepository;
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public PostWithoutCommentsDto createPost(PostRequestDto postRequest, MultipartFile[] imagesFiles){
        
        Long userId = authService.getCurrentUserId();
        List<String> imageUrls = null;

        if (imagesFiles != null && imagesFiles.length != 0) {
            imageUrls = imageService.saveImages(imagesFiles);
        }

        Post post = new Post(
            null,
            userId,
            postRequest.getContent(),
            imageUrls,
            LocalDateTime.now(),
            LocalDateTime.now(),
            postRequest.getTags(),
            0L);
        
        Post savedPost = postRepository.insert(post);
        return getPostWithoutComments(savedPost);
    }

    @Override
    public PostWithoutCommentsDto updatePost(String postId, PostRequestDto postRequest, @RequestPart(value = "imagesFiles", required = false) MultipartFile[] imagesFiles){
        
        Long userId = authService.getCurrentUserId();

        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        if (!existingPost.getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not the owner of this post");
        }

        List<String> newImageUrls = null;

        if (imagesFiles != null && imagesFiles.length != 0) {
            newImageUrls = imageService.saveImages(imagesFiles);

            List<String> oldImages = existingPost.getImages();
            if (oldImages != null) {
                oldImages.forEach(imageService::deleteImage);
            }

            existingPost.setImages(newImageUrls);
        }

        if (postRequest.getContent() != null) {
            existingPost.setContent(postRequest.getContent());
        }

        if (postRequest.getTags() != null) {
            existingPost.setTags(postRequest.getTags());
        }

        existingPost.setUpdatedAt(LocalDateTime.now());

        Post updatedPost = postRepository.save(existingPost);
        return getPostWithoutComments(updatedPost);
    }

    @Override
    public void deletePost(String postId) {
        Long userId = authService.getCurrentUserId();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        if (!post.getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not the owner of this post");
        }

        List<String> images = post.getImages();
        if (images != null && !images.isEmpty()) {
            images.forEach(imageService::deleteImage);
        }

        postRepository.delete(post);
    }

    @Override
    public Page<PostWithoutCommentsDto> getRecommendedPosts(int page, int size) {
        Long currentUserId = authService.getCurrentUserId();
        UserNode user = userNodeRepository.findByUserId(currentUserId);
        Map<String, Integer> userTagCounts = tagService.getUserTagCounts(currentUserId);
    
        Pageable pageable = PageRequest.of(0, 1000);
        List<Post> recentPosts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
    
        Map<Long, BasicInfo> posterMap = getPosterInfo(recentPosts);
    
        List<String> interestedSchools = user.getInterestedSchools().stream()
            .map(SchoolNode::getSchoolName)
            .collect(Collectors.toList());
    
        List<ScoredPostDto> scoredPosts = recentPosts.parallelStream()
            .map(post -> {
                BasicInfo poster = posterMap.get(post.getUserId());
                if (poster != null) {
                    double score = calculateScore(post, poster, user, userTagCounts, interestedSchools);
                    return new ScoredPostDto(post, score);
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    
        scoredPosts.sort((sp1, sp2) -> Double.compare(sp2.getScore(), sp1.getScore()));
    
        int total = scoredPosts.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, total);
    
        List<PostWithoutCommentsDto> paginatedPosts;
    
        if (fromIndex >= total) {
            paginatedPosts = Collections.emptyList();
        } else {
            paginatedPosts = scoredPosts.subList(fromIndex, toIndex).stream()
                .map(scoredPost -> getPostWithoutComments(scoredPost.getPost()))
                .collect(Collectors.toList());
        }
    
        return new PageImpl<>(paginatedPosts, PageRequest.of(page, size), total);
    }

    @Override
    public List<PostWithoutCommentsDto> getPostsByUserId(long userId) {
        List<Post> posts = postRepository.findByUserId(userId);
        return posts.stream()
                    .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                    .map(this::getPostWithoutComments)
                    .collect(Collectors.toList());
    }

    @Override
    public List<PostWithoutCommentsDto> getPostsByKeyword(String keyword){
        List<Post> posts = postRepository.findByContentContaining(keyword);
        return posts.stream()
                    .map(this::getPostWithoutComments)
                    .collect(Collectors.toList());
    }

    @Override
    public PostWithCommentDto getPostWithCommentById(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
    
        BasicInfo postUserInfo = getOrSetBasicInfo(post.getUserId());
    
        PostWithCommentDto postWithCommentDto = createPostWithCommentDto(post, postUserInfo);
    
        List<Comment> comments = commentRepository.findByPostId(postId);
        List<CommentDto> commentDtos = convertToCommentDtoList(comments);
    
        postWithCommentDto.setComments(commentDtos);
        postWithCommentDto.setCommentCount((long) commentDtos.size());
    
        return postWithCommentDto;
    }
        
    @Override
    public CommentDto addComment(String postId, CommentDto commentDto) {
        
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(authService.getCurrentUserId());
        comment.setContent(commentDto.getContent());
        comment.setTimestamp(LocalDateTime.now());
        
        Comment savedComment = commentRepository.save(comment);

        UserNode commentUserNode = userNodeRepository.findByUserId(comment.getUserId());
        String name = commentUserNode != null ? commentUserNode.getName() : "Unknown";

        return new CommentDto(savedComment.getId(), savedComment.getUserId(), name, savedComment.getContent(), savedComment.getTimestamp());
    }

    @Override
    public void deleteComment(String commentId) {

        Long userId = authService.getCurrentUserId();

        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (!comment.getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not the owner of this comment");
        }
        commentRepository.delete(comment);
    }

    @Override
    public void savePost(String postId) {
        Long userId = authService.getCurrentUserId();

        SiteUser user = siteUserRepository.findByUserId(userId);
    
        if (user == null) {
            user = new SiteUser();
            user.setUserId(userId);
            user.setFavorites(new ArrayList<>());
        }
    
        if (!user.getFavorites().contains(postId)) {
            user.getFavorites().add(postId);
            siteUserRepository.save(user);
        }
    }

    @Override
    public void unsavePost(String postId) {

        Long userId = authService.getCurrentUserId();

        SiteUser user = siteUserRepository.findByUserId(userId);
           
        if (user.getFavorites().contains(postId)) {
            user.getFavorites().remove(postId);
            siteUserRepository.save(user);
        }
    }

    @Override
    public List<PostWithoutCommentsDto> getSavedPosts() {
        
        Long userId = authService.getCurrentUserId();
        SiteUser user = siteUserRepository.findByUserId(userId);

        if (user == null) {
            return new ArrayList<>();
        }
    
        if (user.getFavorites().isEmpty()) {
            return new ArrayList<>();
        }

        List<Post> savedPosts = postRepository.findAllById(user.getFavorites());
        return savedPosts.stream()
                .map(this::getPostWithoutComments)
                .collect(Collectors.toList());
    }

    @Override
    public void recordBrowsingHistory(String postId){

        Long userId = authService.getCurrentUserId();
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));

        incrementPostViews(postId);
        logBrowsingHistory(postId, userId);

        tagService.updateUserTagCount(userId, post.getTags());
    }

    @Override
    @Scheduled(fixedRate = 600000)
    public void batchUpdatePostViews() {
    
        Set<String> keys = redisTemplate.keys(POST_VIEWS_KEY + "*");
    
        for (String key : keys) {
            String postId = key.replace(POST_VIEWS_KEY, "");
    
            Object viewsInRedisObj = redisTemplate.opsForValue().get(key);
    
            Long viewsInRedis = null;
            if (viewsInRedisObj instanceof Integer) {
                viewsInRedis = ((Integer) viewsInRedisObj).longValue();
            } else if (viewsInRedisObj instanceof Long) {
                viewsInRedis = (Long) viewsInRedisObj;
            }
    
            if (viewsInRedis != null) {
                Query query = new Query(Criteria.where("_id").is(postId));
                Update update = new Update().set("views", viewsInRedis);
                mongoTemplate.updateFirst(query, update, Post.class);
    
                redisTemplate.delete(key);
            }
        }
    }

    @Override
    @Scheduled(fixedRate = 600000)
    public void batchSaveBrowsingHistory() {

        Set<String> keys = redisTemplate.keys(BROWSING_HISTORY_KEY + "*");
        
        for (String key : keys) {

        List<Object> browsingHistoryObjects = redisTemplate.opsForList().range(key, 0, -1);

        List<BrowsingHistoryDto> browsingHistoryList = browsingHistoryObjects.stream()
            .filter(Objects::nonNull)
            .map(object -> (BrowsingHistoryDto) object)
            .collect(Collectors.toList());

            if (browsingHistoryList != null && !browsingHistoryList.isEmpty()) {

                List<BrowsingHistory> browsingHistories = browsingHistoryList.stream()
                    .map(dto -> new BrowsingHistory(
                            null,
                            dto.getUserId(),
                            dto.getPostId(),
                            LocalDateTime.parse(dto.getTimestampStr())
                        ))
                    .collect(Collectors.toList());

                browsingHistoryRepository.saveAll(browsingHistories);

                redisTemplate.delete(key);
            }
        }
    }

    private BasicInfo getOrSetBasicInfo(Long userId) {
        String userCacheKey = BASIC_INFO_KEY + userId;
        BasicInfo cachedBasicInfo = (BasicInfo) redisTemplate.opsForValue().get(userCacheKey);
    
        if (cachedBasicInfo == null) {
            UserNode userNode = userNodeRepository.findByUserId(userId);
            if (userNode == null) {
                throw new EntityNotFoundException("User not found with id: " + userId);
            }
    
            cachedBasicInfo = new BasicInfo(
                    userNode.getName(),
                    userNode.getPhase(),
                    Optional.ofNullable(userNode.getOriginSchool()).map(school -> school.getSchoolName()).orElse(null),
                    Optional.ofNullable(userNode.getExchangeSchool()).map(school -> school.getSchoolName()).orElse(null),
                    Optional.ofNullable(userNode.getExchangeSchool()).map(school -> school.getNationId()).orElse(null)
            );
    
            redisTemplate.opsForValue().set(userCacheKey, cachedBasicInfo, Duration.ofHours(12));
        }
    
        return cachedBasicInfo;
    }
    
    private PostWithCommentDto createPostWithCommentDto(Post post, BasicInfo basicInfo) {
        PostWithCommentDto dto = new PostWithCommentDto();
        dto.setPostId(post.getId());
        dto.setUserId(post.getUserId());
        dto.setName(basicInfo.getName());
        dto.setPhase(basicInfo.getPhase());
        dto.setOriginSchoolName(basicInfo.getOriginSchoolName());
        dto.setExchangeSchoolName(basicInfo.getExchangeSchoolName());
        dto.setContent(post.getContent());
        dto.setImages(imageService.addImagePrefix(post.getImages()));
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setTags(post.getTags());
        dto.setViews(post.getViews());
        return dto;
    }
    
    private List<CommentDto> convertToCommentDtoList(List<Comment> comments) {
        return comments.stream().map(comment -> {
            BasicInfo commentUserInfo = getOrSetBasicInfo(comment.getUserId());
            String name = commentUserInfo != null ? commentUserInfo.getName() : "Unknown";
    
            return new CommentDto(
                    comment.getId(),
                    comment.getUserId(),
                    name,
                    comment.getContent(),
                    comment.getTimestamp()
            );
        }).collect(Collectors.toList());
    }

    private PostWithoutCommentsDto getPostWithoutComments(Post post) {

        BasicInfo cachedBasicInfo = getOrSetBasicInfo(post.getUserId());

        Long commentCount = commentRepository.countByPostId(post.getId());
    
        return createPostWithoutCommentsDto(post, cachedBasicInfo, commentCount);
    }

    private PostWithoutCommentsDto createPostWithoutCommentsDto(Post post, BasicInfo basicInfo, Long commentCount) {
        PostWithoutCommentsDto dto = new PostWithoutCommentsDto();
        dto.setPostId(post.getId());
        dto.setUserId(post.getUserId());
        dto.setName(basicInfo.getName());
        dto.setPhase(basicInfo.getPhase());
        dto.setOriginSchoolName(basicInfo.getOriginSchoolName());
        dto.setExchangeSchoolName(basicInfo.getExchangeSchoolName());
        dto.setContent(post.getContent());
        dto.setImages(imageService.addImagePrefix(post.getImages()));
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setTags(post.getTags());
        dto.setViews(post.getViews());
        dto.setCommentCount(commentCount);
    
        return dto;
    }
        
    private void incrementPostViews(String postId){

        String redisKey = POST_VIEWS_KEY + postId;

        Object currentViewsObj = redisTemplate.opsForValue().get(redisKey);
        Long currentViews;
    
        if (currentViewsObj instanceof Integer) {
            currentViews = ((Integer) currentViewsObj).longValue();
        } else if (currentViewsObj instanceof Long) {
            currentViews = (Long) currentViewsObj;
        } else {
            currentViews = fetchAndSetPostViewsFromDB(postId);
        }

        redisTemplate.opsForValue().increment(redisKey, 1L);
    }

    private Long fetchAndSetPostViewsFromDB(String postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new EntityNotFoundException("Post not found with id: " + postId));
        Long views = post.getViews() != null ? post.getViews() : 0L;
        redisTemplate.opsForValue().set(POST_VIEWS_KEY + postId, views);
        return views;
    }

    private void logBrowsingHistory(String postId, Long userId){
        
        String redisKey = BROWSING_HISTORY_KEY + userId;
        String timestampStr = LocalDateTime.now().toString();
        BrowsingHistoryDto browsingHistory = new BrowsingHistoryDto(userId, postId, timestampStr);

        redisTemplate.opsForList().rightPush(redisKey, browsingHistory);
    }

    private Map<Long, BasicInfo> getPosterInfo(List<Post> posts) {
        Set<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .collect(Collectors.toSet());
    
        Map<Long, BasicInfo> posterInfoMap = new HashMap<>();
        List<Long> missingUserIds = new ArrayList<>();
    
        for (Long userId : userIds) {
            String cacheKey = BASIC_INFO_KEY + userId;
            BasicInfo cachedBasicInfo = (BasicInfo) redisTemplate.opsForValue().get(cacheKey);
    
            if (cachedBasicInfo != null) {
                posterInfoMap.put(userId, cachedBasicInfo);
            } else {
                missingUserIds.add(userId);
            }
        }
    
        if (!missingUserIds.isEmpty()) {
            List<UserNode> missingPosters = userNodeRepository.findByUserIdIn(missingUserIds);
            
            for (UserNode userNode : missingPosters) {
                BasicInfo basicInfo = new BasicInfo(
                    userNode.getName(),
                    userNode.getPhase(),
                    userNode.getOriginSchool() != null ? userNode.getOriginSchool().getSchoolName() : null,
                    userNode.getExchangeSchool() != null ? userNode.getExchangeSchool().getSchoolName() : null,
                    userNode.getExchangeSchool() != null ? userNode.getExchangeSchool().getNationId() : null
                );
    
                String cacheKey = BASIC_INFO_KEY + userNode.getUserId();
                redisTemplate.opsForValue().set(cacheKey, basicInfo, Duration.ofHours(12));
    
                posterInfoMap.put(userNode.getUserId(), basicInfo);
            }
        }
    
        return posterInfoMap;
    }
    

    private double calculateScore(Post post, BasicInfo poster, UserNode user, Map<String, Integer> userTagCounts, List<String> interestedSchools) {
        double score = 0;
    
        long timeDifference = ChronoUnit.SECONDS.between(post.getCreatedAt(), LocalDateTime.now());
        double timeScore = Math.exp(-lambda * timeDifference);
        score += timeScore;
    
        score += calculateSchoolScore(poster, user, interestedSchools);
    
        double tagScore = 0;
        List<String> tags = post.getTags();
        if (tags != null) {
            for (String tag : tags) {
                int tagCount = userTagCounts.getOrDefault(tag, 0);
                tagScore += tagCount * tagWeight;
            }
        }
        score += tagScore;
    
        return score;
    }
    
    private double calculateSchoolScore(BasicInfo poster, UserNode user, List<String> interestedSchools) {
        double schoolScore = 0;
        String posterExchangeSchool = poster.getExchangeSchoolName();
        String userExchangeSchool = user.getExchangeSchool() != null ? user.getExchangeSchool().getSchoolName() : null;
    
        if (user.getPhase() != null && user.getPhase().equals("APPLYING")) {
            if (posterExchangeSchool != null && interestedSchools.contains(posterExchangeSchool)) {
                schoolScore += highSchoolScore;
            }
        } else {
            if (posterExchangeSchool != null && userExchangeSchool != null) {
                if (posterExchangeSchool.equals(userExchangeSchool)) {
                    schoolScore += highSchoolScore;
                } else if (user.getExchangeSchool() != null && poster.getNationId() != null &&
                    user.getExchangeSchool().getNationId().equals(poster.getNationId())) {
                    schoolScore += mediumCountryScore;
                }
            }
        }
        return schoolScore;
    }
}