package com.example.social_media.repository.neo4j;

import com.example.social_media.dto.friend.UserFriendResultDto;
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
        MATCH (u:UserNode {userId: $userId})-[:FRIENDS]->(f:UserNode)
        OPTIONAL MATCH (f)-[:ORIGIN_IN]->(originSchool:SchoolNode)
        OPTIONAL MATCH (f)-[:EXCHANGE_TO]->(exchangeSchool:SchoolNode)
        
        RETURN 
            f.userId AS userId, 
            f.name AS name, 
            f.phase AS phase,
            originSchool.schoolName AS originSchoolName,
            exchangeSchool.schoolName AS exchangeSchoolName
    """)
    List<UserFriendResultDto> findFriendsInfo(Long userId);
    
    @Query("""
        MATCH (target:UserNode {userId: $targetUserId})

        CALL {
            WITH target
            MATCH (u:UserNode {userId: $currentUserId})
            OPTIONAL MATCH (u)-[:FRIENDS]->(mutualFriend:UserNode)<-[:FRIENDS]-(target)
            RETURN collect(DISTINCT mutualFriend.name) AS mutualFriends
        }

        CALL {
            WITH target
            MATCH (u:UserNode {userId: $currentUserId})
            OPTIONAL MATCH (u)-[:INTERESTED_IN]->(mutualInterest:InterestNode)<-[:INTERESTED_IN]-(target)
            RETURN collect(DISTINCT mutualInterest.interest) AS mutualInterests
        }

        MATCH (u:UserNode {userId: $currentUserId})
        OPTIONAL MATCH (u)-[friendRel:FRIENDS]->(target)
        OPTIONAL MATCH (u)-[sentRel:FRIEND_REQUEST_SENT]->(target)
        OPTIONAL MATCH (u)-[receivedRel:FRIEND_REQUEST_RECEIVED]->(target)

        WITH target, mutualFriends, mutualInterests,
            CASE 
                WHEN friendRel IS NOT NULL THEN 'FRIENDS'
                WHEN sentRel IS NOT NULL THEN 'FRIEND_REQUEST_SENT'
                WHEN receivedRel IS NOT NULL THEN 'FRIEND_REQUEST_RECEIVED'
                ELSE 'NO_RELATION'
            END AS relationship

        RETURN target.userId AS userId, 
            target.name AS name, 
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
        MATCH (u:UserNode {userId: $userId})
        MATCH (u)-[:INTERESTED_SCHOOL|EXCHANGE_TO]->(school:SchoolNode)

        MATCH (target:UserNode)
        WHERE target.userId <> u.userId
        AND 
        (
            (target)-[:ORIGIN_IN]->(school)
            OR
            (target)-[:EXCHANGE_TO]->(school)
        )

        RETURN DISTINCT target.userId
    """)    
    List<Long> findUsersCommonSchool(Long userId);

    @Query("""
        MERGE (u:UserNode {userId: $userId})
        MERGE (target:UserNode {userId: $targetUserId})
        MERGE (u)-[:FRIEND_REQUEST_SENT]->(target)
        MERGE (target)-[:FRIEND_REQUEST_RECEIVED]->(u)
        """)
    void sendFriendRequest(Long userId, Long targetUserId);
    
    @Query("""
        MATCH (u:UserNode {userId: $userId})-[r1:FRIEND_REQUEST_RECEIVED]->(target:UserNode {userId: $targetUserId}),
            (target)-[r2:FRIEND_REQUEST_SENT]->(u)
        DELETE r1, r2
        MERGE (u)-[:FRIENDS]->(target)
        MERGE (target)-[:FRIENDS]->(u)
        """)
    void acceptFriendRequest(Long userId, Long targetUserId);
    
    @Query("""
        MATCH (u:UserNode {userId: $userId})-[r1:FRIEND_REQUEST_RECEIVED]->(target:UserNode {userId: $targetUserId}),
            (target)-[r2:FRIEND_REQUEST_SENT]->(u)
        DELETE r1, r2
        """)
    void rejectFriendRequest(Long userId, Long targetUserId);

    @Query("""
        MATCH (target:UserNode {userId: $userId})-[:FRIEND_REQUEST_RECEIVED]->(sender:UserNode)
        OPTIONAL MATCH (sender)-[:ORIGIN_IN]->(originSchool:SchoolNode)
        OPTIONAL MATCH (sender)-[:EXCHANGE_TO]->(exchangeSchool:SchoolNode)

        RETURN 
            sender.userId AS userId, 
            sender.name AS name,
            sender.phase AS phase,
            originSchool.schoolName AS originSchoolName,
            exchangeSchool.schoolName AS exchangeSchoolName
    """)
    List<UserFriendResultDto> findPendingFriendRequests(Long userId);

}