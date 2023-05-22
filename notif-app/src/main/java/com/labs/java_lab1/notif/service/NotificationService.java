package com.labs.java_lab1.notif.service;

import com.labs.java_lab1.common.dto.NotifTypeEnum;
import com.labs.java_lab1.common.exception.DateParseException;
import com.labs.java_lab1.common.security.JwtUserData;
import com.labs.java_lab1.notif.dto.NotifListDto;
import com.labs.java_lab1.notif.dto.NotifListPaginationDto;
import com.labs.java_lab1.notif.dto.StatusChangeDto;
import com.labs.java_lab1.notif.dto.UnreadDto;
import com.labs.java_lab1.notif.entity.NotificationEntity;
import com.labs.java_lab1.notif.entity.StatusEnum;
import com.labs.java_lab1.notif.exception.NotificationNotFoundException;
import com.labs.java_lab1.notif.exception.TypeNotFoundException;
import com.labs.java_lab1.notif.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    /**
     * Подсчёт непрочитанных сообщений
     * @return количество непрочитанных сообщений
     */
    public UnreadDto countUnreadMessages() {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String login = ((JwtUserData)authentication).getLogin();

        log.info("Counted unread messages");
        return new UnreadDto(
                repository.countByUserIdAndStatusAndType(login, StatusEnum.UNREAD, NotifTypeEnum.NEW_MESSAGE)
        );
    }

    /**
     * Изменение статуса представленного списка уведомлений
     * @param dto дто со списком id уведомлений и их статусом
     * @return дто с количеством непрочтённых сообщений
     */
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
        log.info("Notification status changed");
        return countUnreadMessages();
    }

    /**
     * Список уведомлений
     * @param dto дто с пагинацией и параметрами фильрации
     * @return список уведомлений
     */
    public List<NotifListDto> notifList(NotifListPaginationDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String login = ((JwtUserData)authentication).getLogin();

        //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date startDate = null;
        Date endDate = null;

        if (dto.getStartDate() != null) {
            log.info("Start date is null");
            try {
                startDate = java.util.Date.from(Instant.parse(dto.getStartDate()));
            } catch (Exception e) {
                log.error("Invalid date format");
                throw new DateParseException("Invalid date format");
            }
        }

        if (dto.getEndDate() != null) {
            log.info("End date is null");
            try {
                endDate = java.util.Date.from(Instant.parse(dto.getEndDate()));
            } catch (Exception e) {
                log.error("Invalid date format");
                throw new DateParseException("Invalid date format");
            }
        }

        List<NotifTypeEnum> notifTypeEnumList = new ArrayList<>();
        for (String typeCheck : dto.getTypes()) {
            NotifTypeEnum typeEnum = null;
            for (NotifTypeEnum type : NotifTypeEnum.values()) {
                if (type.name().equalsIgnoreCase(typeCheck)) {
                    typeEnum = type;
                }
            }
            if (typeEnum == null) {
                log.error("Type is not found");
                throw new TypeNotFoundException("Type is not found");
            }
            notifTypeEnumList.add(typeEnum);
        }
        if (notifTypeEnumList.isEmpty()) {
            notifTypeEnumList = null;
        }

        if (dto.getPageNo() == null) {
            dto.setPageNo(1);
        }

        if (dto.getPageSize() == null) {
            dto.setPageSize(10);
        }

        PageRequest request = PageRequest.of(dto.getPageNo() - 1, dto.getPageSize());
        return repository.getAllByFilters(login, startDate, endDate, dto.getText(), notifTypeEnumList, request);
    }
}
