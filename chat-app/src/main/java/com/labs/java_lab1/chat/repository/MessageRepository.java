package com.labs.java_lab1.chat.repository;

import com.labs.java_lab1.chat.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, String> {

}
