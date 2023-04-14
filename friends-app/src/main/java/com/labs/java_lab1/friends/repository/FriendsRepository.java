package com.labs.java_lab1.friends.repository;

import com.labs.java_lab1.friends.entity.FriendsEntity;
import lombok.NonNull;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendsRepository extends JpaRepository<FriendsEntity, String> {
    Optional<FriendsEntity> getByUserIdAndFriendId(String userId, String friendId);
    Page<FriendsEntity> findAllByUserIdAndFriendNameContaining(String userId, String friendName, Pageable pageable);
    @NonNull Page<FriendsEntity> findAll(@NonNull Example example, @NonNull Pageable pageable);

}
