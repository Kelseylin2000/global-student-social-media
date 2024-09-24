package com.example.social_media.repository.neo4j;

import com.example.social_media.model.node.SchoolNode;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface SchoolNodeRepository extends Neo4jRepository<SchoolNode, Long> {
    List<SchoolNode> findBySchoolIdIn(List<Integer> schoolIds);
}