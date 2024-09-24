package com.example.social_media.model.node;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import com.example.social_media.model.enumtype.Phase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Node
public class UserNode {

    @Id
    @GeneratedValue
    private Long id;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserNode userNode = (UserNode) o;
        return Objects.equals(id, userNode.id);
    }
    
    private Long userId;
    private String name;
    private Phase phase;
    private LocalDate startDate;

    @Relationship(type = "FRIEND_REQUEST_SENT")
    private Set<UserNode> friendRequestSent;

    @Relationship(type = "FRIEND_REQUEST_RECEIVED")
    private Set<UserNode> friendRequestReceived;

    @Relationship(type = "FRIENDS")
    private Set<UserNode> friends;

    @Relationship(type = "INTERESTED_IN")
    private Set<InterestNode> interests;

    @Relationship(type = "ORIGIN_IN")
    private SchoolNode originSchool;

    @Relationship(type = "EXCHANGE_TO")
    private SchoolNode exchangeSchool;

    @Relationship(type = "INTERESTED_IN")
    private Set<SchoolNode> interestedSchools;
}
