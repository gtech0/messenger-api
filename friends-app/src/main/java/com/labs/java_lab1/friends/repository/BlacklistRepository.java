package com.labs.java_lab1.friends.repository;

import com.labs.java_lab1.friends.entity.BlacklistEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlacklistRepository extends JpaRepository<BlacklistEntity, String> {
    Optional<BlacklistEntity> getByUserIdAndFriendId(String userId, String friendId);
    List<BlacklistEntity> getAllByFriendId(String friendId);
    List<BlacklistEntity> findAllByUserIdAndDeleteDateAndFriendNameContaining
            (String userId, Date deleteDate, String friendName, Pageable pageable);
    @Query(value =
            "SELECT b " +
                    "FROM BlacklistEntity b " +
                    "WHERE (b.addDate = :addDate OR cast(:addDate as timestamp) IS NULL) " +
                    "AND b.deleteDate IS NULL " +
                    "AND b.userId = :userId " +
                    "AND (b.friendId = :friendId OR :friendId IS NULL) " +
                    "AND (b.friendName LIKE CONCAT('%', :friendName, '%') OR :friendName IS NULL)")
    List<BlacklistEntity> findAllQuery(Date addDate,
                                     String userId,
                                     String friendId,
                                     String friendName,
                                     Pageable pageable);

}
