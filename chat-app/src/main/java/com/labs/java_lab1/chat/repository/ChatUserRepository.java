package com.labs.java_lab1.chat.repository;

import com.labs.java_lab1.chat.entity.ChatUserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatUserRepository extends JpaRepository<ChatUserEntity, String> {
    Optional<ChatUserEntity> getByChatIdAndUserId(String chatId, String userId);
    void deleteAllByChatIdAndUserIdIn(String chatId, List<String> users);
    List<ChatUserEntity> getAllByUserId(String userId, Pageable pageable);
    List<ChatUserEntity> getAllByUserId(String userId);
    @Query("SELECT cu.userId FROM ChatUserEntity cu WHERE cu.chatId = :chatId")
    List<String> getAllByChatIdQuery(String chatId);
}
