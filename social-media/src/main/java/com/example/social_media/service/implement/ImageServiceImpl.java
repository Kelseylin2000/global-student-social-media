package com.example.social_media.service.implement;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import com.example.social_media.service.ImageService;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;


@Service
public class ImageServiceImpl implements ImageService{

    private final String bucketName;
    private final String region;
    private final String storageLocation;
    private final S3Client s3Client;
    private final String imagePrefix;

    public ImageServiceImpl(
            @Value("${aws.s3.bucketName}") String bucketName,
            @Value("${aws.s3.region}") String region,
            @Value("${storage.location}") String storageLocation,
            @Value("${image.prefix}") String imagePrefix) {
        this.bucketName = bucketName;
        this.region = region;
        this.storageLocation = storageLocation;
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .build();
        this.imagePrefix = imagePrefix;
    }

    @Override
    public String saveImage(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }

        try {
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String newFilename = UUID.randomUUID().toString() + "-" + originalFilename;

            String s3Key = storageLocation + "/" + newFilename;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            PutObjectResponse response = s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

            if (response.sdkHttpResponse().isSuccessful()) {
                return s3Key;
            } else {
                return null;
            }

        } catch (S3Exception | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<String> saveImages(MultipartFile[] files) {
        return Arrays.stream(files)
            .map(this::saveImage)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteImage(String s3Key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public List<String> addImagePrefix(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return imageUrls;
        }

        return imageUrls.stream()
                        .map(url -> imagePrefix + url)
                        .collect(Collectors.toList());
    }
}