package com.example.social_media.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String saveImage(MultipartFile file);
    List<String> saveImages(MultipartFile[] files);
    void deleteImage(String s3Key);
    List<String> addImagePrefix(List<String> imageUrls);
}

