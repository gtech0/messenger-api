package com.labs.java_lab1.chat.repository;

import com.labs.java_lab1.chat.dto.QueryMessageSearchDto;
import com.labs.java_lab1.chat.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, String> {
    Optional<MessageEntity> getByUuid(String id);
    List<MessageEntity> getAllByUserId(String userId);
    List<MessageEntity> getAllByChatIdOrderBySentDateDesc(String chatId);
    Optional<MessageEntity> getFirstByChatIdOrderBySentDateDesc(String chatId);
    @Query(value =
            "SELECT DISTINCT new com.labs.java_lab1.chat.dto.QueryMessageSearchDto(" +
                    "msg.chatId, c.name, msg.message, msg.sentDate, " +
                    "c.type, msg.userId, msg.fullName, msg.uuid) " +
                    "FROM MessageEntity msg " +
                    "JOIN AttachmentEntity atch ON msg.uuid = atch.message.uuid " +
                    "JOIN ChatEntity c ON msg.chatId = c.uuid " +
                    "JOIN ChatUserEntity cu ON c.uuid = cu.chatId " +
                    "WHERE cu.userId = :userId " +
                    "AND c.uuid = :chatId " +
                    "AND (msg.message LIKE CONCAT('%', :message, '%') " +
                    "OR atch.fileName LIKE CONCAT('%', :message, '%'))")
    List<QueryMessageSearchDto> getAllByChatIdAndMessageLike(String userId, String chatId, String message);
}
