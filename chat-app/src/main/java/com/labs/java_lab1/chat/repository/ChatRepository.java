package com.labs.java_lab1.chat.repository;

import com.labs.java_lab1.chat.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, String> {
    Optional<ChatEntity> getByUserIdAndFriendId(String userId, String friendId);
    Optional<ChatEntity> getByUuid(String id);
}
