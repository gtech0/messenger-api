package com.labs.java_lab1.notif.stream;

import com.labs.java_lab1.common.dto.NotifDto;
import com.labs.java_lab1.notif.entity.NotificationEntity;
import com.labs.java_lab1.notif.entity.StatusEnum;
import com.labs.java_lab1.notif.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitUserEventListener {

    private final NotificationRepository repository;

    @Bean
    public Consumer<NotifDto> userModifiedEvent() {
        return message -> {
            repository.save(new NotificationEntity(
                    UUID.randomUUID().toString(),
                    message.getType(),
                    message.getNotifMessage(),
                    message.getUserId(),
                    StatusEnum.UNREAD,
                    new Date()
            ));
            log.info("Сообщение: {}", message);
        };
    }
    //return message -> log.info("Сообщение: {}", message);
}
