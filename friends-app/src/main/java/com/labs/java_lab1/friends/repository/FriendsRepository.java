package com.labs.java_lab1.friends.repository;

import com.labs.java_lab1.friends.entity.FriendsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendsRepository extends JpaRepository<FriendsEntity, String> {
    Optional<FriendsEntity> getByUserIdAndFriendId(String userId, String friendId);

}
