package com.labs.java_lab1.chat.repository;

import com.labs.java_lab1.chat.entity.ChatUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatUserRepository extends JpaRepository<ChatUserEntity, String> {

}
