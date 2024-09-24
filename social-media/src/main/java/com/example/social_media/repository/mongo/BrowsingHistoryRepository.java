package com.example.social_media.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.social_media.model.document.BrowsingHistory;

public interface BrowsingHistoryRepository extends MongoRepository<BrowsingHistory, String>{
}
