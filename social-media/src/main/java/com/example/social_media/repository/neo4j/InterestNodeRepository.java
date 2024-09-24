package com.example.social_media.repository.neo4j;

import com.example.social_media.model.node.InterestNode;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface InterestNodeRepository extends Neo4jRepository<InterestNode, Long> {
    InterestNode findByInterestId(Integer interestId);
    List<InterestNode> findByInterestIdIn(List<Integer> interestIds);
}