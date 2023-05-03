package com.labs.java_lab1.chat.repository;

import com.labs.java_lab1.chat.entity.ChatUserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatUserRepository extends JpaRepository<ChatUserEntity, String> {
    Optional<ChatUserEntity> getByChatIdAndUserId(String chatId, String userId);
    void deleteAllByChatIdAndUserIdNotIn(String chatId, List<String> users);
    List<ChatUserEntity> getAllByUserId(String userId, @PageableDefault(size = 50) Pageable pageable);
    List<ChatUserEntity> getAllByUserId(String userId);
}
