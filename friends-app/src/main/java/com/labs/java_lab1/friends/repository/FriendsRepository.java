package com.labs.java_lab1.friends.repository;

import com.labs.java_lab1.friends.entity.FriendsEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendsRepository extends JpaRepository<FriendsEntity, String> {
    Optional<FriendsEntity> getByUserIdAndFriendId(String userId, String friendId);
    List<FriendsEntity> getAllByFriendId(String friendId);
    List<FriendsEntity> findAllByUserIdAndDeleteDateAndFriendNameContaining
            (String userId, Date deleteDate, String friendName, Pageable pageable);
    @Query(value =
            "SELECT f " +
                    "FROM FriendsEntity f " +
                    "WHERE (f.addDate = :addDate OR cast(:addDate as timestamp) IS NULL) " +
                    "AND f.deleteDate IS NULL " +
                    "AND f.userId = :userId " +
                    "AND (f.friendId = :friendId OR :friendId IS NULL) " +
                    "AND (f.friendName LIKE CONCAT('%', :friendName, '%') OR :friendName IS NULL)")
    List<FriendsEntity> findAllQuery(Date addDate,
                                     String userId,
                                     String friendId,
                                     String friendName,
                                     Pageable pageable);

}
