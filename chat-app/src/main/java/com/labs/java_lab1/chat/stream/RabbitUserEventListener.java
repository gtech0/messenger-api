package com.labs.java_lab1.chat.stream;

import com.labs.java_lab1.chat.entity.MessageEntity;
import com.labs.java_lab1.chat.repository.MessageRepository;
import com.labs.java_lab1.common.dto.UserSyncDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitUserEventListener {

    private final MessageRepository messageRepository;

    @Bean
    public Consumer<UserSyncDto> userModifiedEvent() {
        return message -> {
            List<MessageEntity> messageList = messageRepository.getAllByUserId(message.getUserId());
            for (MessageEntity messageEntity : messageList) {
                messageEntity.setFullName(message.getFullName());
                messageEntity.setAvatar(message.getAvatar());
                messageRepository.save(messageEntity);
            }
        };
    }
}
