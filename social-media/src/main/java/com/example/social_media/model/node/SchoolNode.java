package com.example.social_media.model.node;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Node
public class SchoolNode {

    @Id
    @GeneratedValue
    private Long id;

    private Long schoolId;
    private String schoolName;
    private Integer nationId;
}