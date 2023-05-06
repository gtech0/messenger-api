package com.labs.java_lab1.notif.service;

import com.labs.java_lab1.common.dto.NotifTypeEnum;
import com.labs.java_lab1.common.security.JwtUserData;
import com.labs.java_lab1.notif.dto.StatusChangeDto;
import com.labs.java_lab1.notif.dto.UnreadDto;
import com.labs.java_lab1.notif.entity.NotificationEntity;
import com.labs.java_lab1.notif.entity.StatusEnum;
import com.labs.java_lab1.notif.exception.NotificationNotFoundException;
import com.labs.java_lab1.notif.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    public UnreadDto countUnreadMessages() {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String login = ((JwtUserData)authentication).getLogin();

        return new UnreadDto(
                repository.countByUserIdAndStatusAndType(login, StatusEnum.UNREAD, NotifTypeEnum.NEW_MESSAGE)
        );
    }

    @Transactional
    public UnreadDto changeNotifStatus(StatusChangeDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String login = ((JwtUserData)authentication).getLogin();

        for (String notificationId : dto.getNotifications()) {
            Optional<NotificationEntity> optionalNotification = repository.getByUuidAndUserId(notificationId, login);

            if (optionalNotification.isEmpty()) {
                throw new NotificationNotFoundException("Notification doesn't exist");
            }

            NotificationEntity notification = optionalNotification.get();
            notification.setStatus(dto.getStatus());
            repository.save(notification);
        }
        return countUnreadMessages();
    }

}
