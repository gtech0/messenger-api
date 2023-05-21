package com.labs.java_lab1.notif.service;

import com.labs.java_lab1.common.dto.NotifTypeEnum;
import com.labs.java_lab1.common.security.JwtUserData;
import com.labs.java_lab1.notif.dto.NotifListDto;
import com.labs.java_lab1.notif.dto.NotifListPaginationDto;
import com.labs.java_lab1.notif.dto.StatusChangeDto;
import com.labs.java_lab1.notif.dto.UnreadDto;
import com.labs.java_lab1.notif.entity.NotificationEntity;
import com.labs.java_lab1.notif.entity.StatusEnum;
import com.labs.java_lab1.notif.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Тестирование методов сервиса уведомлений
 * с помощью сравнения реального и ожидаемого результата
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class NotificationServiceTest {

    @InjectMocks
    private NotificationService service;

    @Mock
    private NotificationRepository repository;

    @Test
    public void countUnreadMessages() {
        UUID userId = UUID.fromString("540af0db-4862-40f1-8934-385f4da10ae1");

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = mock(Authentication.class);
        JwtUserData jwtUserData = new JwtUserData(userId,"gqwrhrehr", "Иванов Иван Иваныч");
        when(authentication.getPrincipal()).thenReturn(jwtUserData);
        context.setAuthentication(authentication);

        when(repository.countByUserIdAndStatusAndType("gqwrhrehr", StatusEnum.UNREAD, NotifTypeEnum.NEW_MESSAGE))
                .thenReturn(10);

        UnreadDto unreadDto = new UnreadDto(10);

        assert Objects.equals(service.countUnreadMessages(), unreadDto);
    }

    @Test
    public void changeNotifStatus() {
        UUID userId = UUID.fromString("540af0db-4862-40f1-8934-385f4da10ae1");

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = mock(Authentication.class);
        JwtUserData jwtUserData = new JwtUserData(userId,"gqwrhrehr", "Иванов Иван Иваныч");
        when(authentication.getPrincipal()).thenReturn(jwtUserData);
        context.setAuthentication(authentication);

        String notifId = "140af0db-4862-40f1-8934-385f4da10ae1";
        when(repository.getByUuidAndUserId(notifId, "gqwrhrehr"))
                .thenReturn(Optional.of(notificationEntity()));

        when(repository.countByUserIdAndStatusAndType("gqwrhrehr", StatusEnum.UNREAD, NotifTypeEnum.NEW_MESSAGE))
                .thenReturn(0);

        List<String> statusList = new ArrayList<>();
        statusList.add(notifId);

        StatusChangeDto statusChangeDto = new StatusChangeDto(
                statusList,
                StatusEnum.READ
        );
        UnreadDto unreadDto = new UnreadDto(0);

        assert Objects.equals(service.changeNotifStatus(statusChangeDto), unreadDto);
    }

    @Test
    public void notifList() {
        UUID userId = UUID.fromString("540af0db-4862-40f1-8934-385f4da10ae1");

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = mock(Authentication.class);
        JwtUserData jwtUserData = new JwtUserData(userId,"gqwrhrehr", "Иванов Иван Иваныч");
        when(authentication.getPrincipal()).thenReturn(jwtUserData);
        context.setAuthentication(authentication);

        NotifListPaginationDto notifListPaginationDto = new NotifListPaginationDto(
                1,
                1,
                null,
                null,
                "",
                List.of(NotifTypeEnum.LOG_IN.name())
        );

        PageRequest request = PageRequest.of(0, 1);
        when(repository.getAllByFilters("gqwrhrehr", null, null, "", List.of(NotifTypeEnum.LOG_IN), request))
                .thenReturn(List.of(notifListDto()));

        assert Objects.equals(service.notifList(notifListPaginationDto), List.of(notifListDto()));
    }

    @SneakyThrows
    private NotificationEntity notificationEntity() {
        return new NotificationEntity(
                "140af0db-4862-40f1-8934-385f4da10ae1",
                NotifTypeEnum.NEW_MESSAGE,
                "",
                "540af0db-4862-40f1-8934-385f4da10ae1",
                StatusEnum.UNREAD,
                new Date(123)
        );
    }

    @SneakyThrows
    private NotifListDto notifListDto() {
        return new NotifListDto(
                "140af0db-4862-40f1-8934-385f4da10ae1",
                NotifTypeEnum.LOG_IN,
                "",
                StatusEnum.UNREAD,
                null
        );
    }
}
