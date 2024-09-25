package com.example.social_media.repository.neo4j;

import com.example.social_media.dto.friend.PendingFriendRequestDto;
import com.example.social_media.dto.user.TargetUserProfileDto;
import com.example.social_media.dto.user.UserNodeWithMutualInfoDto;
import com.example.social_media.model.node.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import java.util.List;
import java.util.Set;

public interface UserNodeRepository extends Neo4jRepository<UserNode, Long> {
    
    UserNode findByUserId(Long userId);
    List<UserNode> findByUserIdIn(Set<Long> userIds);
    
    @Query("""
        MATCH (u:UserNode {userId: $currentUserId}), (target:UserNode {userId: $targetUserId})
        
        OPTIONAL MATCH (target)-[:INTERESTED_IN]->(interest:InterestNode)
        OPTIONAL MATCH (u)-[:FRIENDS]->(mutualFriend:UserNode)<-[:FRIENDS]-(target)
        OPTIONAL MATCH (u)-[:INTERESTED_IN]->(mutualInterest:InterestNode)<-[:INTERESTED_IN]-(target)
        OPTIONAL MATCH (u)-[friendRel:FRIENDS]->(target)
        OPTIONAL MATCH (u)-[sentRel:FRIEND_REQUEST_SENT]->(target)
        OPTIONAL MATCH (u)-[receivedRel:FRIEND_REQUEST_RECEIVED]->(target)
        OPTIONAL MATCH (target)-[:ORIGIN_IN]->(originSchool:SchoolNode)
        OPTIONAL MATCH (target)-[:EXCHANGE_TO]->(exchangeSchool:SchoolNode)

    
        WITH target,
            originSchool,
            exchangeSchool,
             collect(DISTINCT interest.interest) AS interests,
             collect(DISTINCT mutualFriend.name) AS mutualFriends,
             collect(DISTINCT mutualInterest.interest) AS mutualInterests,
             CASE 
                 WHEN friendRel IS NOT NULL THEN 'FRIENDS'
                 WHEN sentRel IS NOT NULL THEN 'FRIEND_REQUEST_SENT'
                 WHEN receivedRel IS NOT NULL THEN 'FRIEND_REQUEST_RECEIVED'
                 ELSE 'NO_RELATION'
             END AS relationship
    
        RETURN target.userId AS userId, 
               target.name AS name, 
               target.phase AS phase,
               originSchool.schoolName AS originSchoolName, 
               exchangeSchool.schoolName AS exchangeSchoolName, 
               interests,
               mutualFriends, 
               mutualInterests, 
               relationship
        """)
    TargetUserProfileDto findUserProfileWithMutualInfo(Long currentUserId, Long targetUserId);
    

    @Query("""
        MATCH (target:UserNode {userId: $targetUserId})-[:INTERESTED_IN]->(interest:InterestNode)
        RETURN interest.interest
    """)
    List<String> findUserInterestsByUserId(Long targetUserId);

    @Query("""
        MATCH (target:UserNode)
        OPTIONAL MATCH (u:UserNode)-[:FRIENDS]->(mutualFriend:UserNode)<-[:FRIENDS]-(target)
        OPTIONAL MATCH (u:UserNode)-[:INTERESTED_IN]->(mutualInterest:InterestNode)<-[:INTERESTED_IN]-(target)
        WHERE target.name CONTAINS $keyword
        RETURN target,
               collect(DISTINCT mutualFriend.name) AS mutualFriends,
               collect(DISTINCT mutualInterest.interest) AS mutualInterests
        """)
    List<UserNodeWithMutualInfoDto> findUsersByNameWithDetails(String keyword);

    @Query("""
        MATCH (u:UserNode {userId: :userId})
        MATCH (target:UserNode)
        OPTIONAL MATCH (target)-[:ORIGIN_IN]->(originSchool:SchoolNode)
        OPTIONAL MATCH (target)-[:EXCHANGE_TO]->(exchangeSchool:SchoolNode)
        WHERE 
        (
            (u.phase = 'APPLYING' AND (u)-[:INTERESTED_IN]->(originSchool))            
            OR
            (u.phase IN ['ADMITTED', 'STUDY_ABROAD', 'RETURNED'] AND (u)-[:EXCHANGE_TO]->(originSchool))
        )
        AND target.userId <> u.userId

        OPTIONAL MATCH (u)-[:FRIENDS]->(commonFriend:UserNode)<-[:FRIENDS]-(target)
        OPTIONAL MATCH (u)-[:INTERESTED_IN]->(commonInterest:InterestNode)<-[:INTERESTED_IN]-(target)

        RETURN target, 
               collect(DISTINCT commonFriend.name) AS mutualFriends, 
               collect(DISTINCT commonInterest.interest) AS mutualInterests
        """)
    List<UserNodeWithMutualInfoDto> findUsersFromSpecificSchool(Long userId);

    @Query("""
        MATCH (u:UserNode {userId: :userId})
        MATCH (target:UserNode)
        OPTIONAL MATCH (target)-[:ORIGIN_IN]->(originSchool:SchoolNode)
        OPTIONAL MATCH (target)-[:EXCHANGE_TO]->(exchangeSchool:SchoolNode)
        WHERE 
        (
            (u.phase = 'APPLYING' AND (u)-[:INTERESTED_IN]->(exchangeSchool))            
            OR
            (u.phase IN ['ADMITTED', 'STUDY_ABROAD', 'RETURNED'] AND (u)-[:EXCHANGE_TO]->(exchangeSchool))
        )
        AND 
        (
            NOT (target)-[:EXCHANGE_TO]->(exchangeSchool) 
            OR target.startDate <= date().minusMonths(3)
        )
        AND target.userId <> u.userId

        OPTIONAL MATCH (u)-[:FRIENDS]->(commonFriend:UserNode)<-[:FRIENDS]-(target)
        OPTIONAL MATCH (u)-[:INTERESTED_IN]->(commonInterest:InterestNode)<-[:INTERESTED_IN]-(target)

        RETURN target, 
               collect(DISTINCT commonFriend.name) AS mutualFriends, 
               collect(DISTINCT commonInterest.interest) AS mutualInterests
        """)
    List<UserNodeWithMutualInfoDto> findUsersToSpecificSchool(Long userId);

    @Query("""
        MERGE (u:UserNode {userId: :userId})
        MERGE (target:UserNode {userId: :targetUserId})
        MERGE (u)-[:FRIEND_REQUEST_SENT]->(target)
        MERGE (target)-[:FRIEND_REQUEST_RECEIVED]->(u)
        RETURN u, target
        """)
    void sendFriendRequest(Long userId, Long targetUserId);

    @Query("""
        MATCH (u:UserNode {userId: :userId})-[r1:FRIEND_REQUEST_RECEIVED]->(target:UserNode {userId: :targetUserId}),
            (target)-[r2:FRIEND_REQUEST_SENT]->(u)
        DELETE r1, r2
        MERGE (u)-[:FRIENDS]->(target)
        MERGE (target)-[:FRIENDS]->(u)
        RETURN u, target
        """)
    void acceptFriendRequest(Long userId, Long targetUserId);

    @Query("""
        MATCH (u:UserNode {userId: :userId})-[r1:FRIEND_REQUEST_RECEIVED]->(target:UserNode {userId: :targetUserId}),
            (target)-[r2:FRIEND_REQUEST_SENT]->(u)
        DELETE r1, r2
        RETURN u, target
        """)
    void rejectFriendRequest(Long userId, Long targetUserId);

    @Query("""
    MATCH (target:UserNode {userId: :userId})<-[:FRIEND_REQUEST_RECEIVED]-(sender:UserNode)
    RETURN sender.userId AS senderId, sender.name AS senderName
    """)
    List<PendingFriendRequestDto> findPendingFriendRequests(Long userId);

}