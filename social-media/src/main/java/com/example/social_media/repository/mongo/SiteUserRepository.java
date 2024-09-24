package com.example.social_media.repository.mongo;

import com.example.social_media.model.document.SiteUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SiteUserRepository extends MongoRepository<SiteUser, String>{
    SiteUser findByUserId(Long userId);
}
