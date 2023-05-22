package com.labs.java_lab1.chat.service;

import com.labs.java_lab1.chat.dto.ChatInfoDto;
import com.labs.java_lab1.chat.dto.MessageInfoDto;
import com.labs.java_lab1.chat.entity.*;
import com.labs.java_lab1.chat.repository.ChatRepository;
import com.labs.java_lab1.chat.repository.ChatUserRepository;
import com.labs.java_lab1.chat.repository.MessageRepository;
import com.labs.java_lab1.common.dto.FileIdNameSizeDto;
import com.labs.java_lab1.common.security.JwtUserData;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ChatUserRepository chatUserRepository;

    @Mock
    private MessageRepository messageRepository;

    @Test
    public void chatInfo() {
        String chatId = "d36580ba-5b4d-4a48-a84a-0362261a2060";
        UUID userId = UUID.fromString("540af0db-4862-40f1-8934-385f4da10ae1");

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = mock(Authentication.class);
        JwtUserData jwtUserData = new JwtUserData(userId,"gqwrhrehr", "Иванов Иван Иваныч");
        when(authentication.getPrincipal()).thenReturn(jwtUserData);
        context.setAuthentication(authentication);

        when(chatRepository.getByUuid(chatId))
                .thenReturn(Optional.of(chatEntity()));

        ChatInfoDto chatInfoDtoTest = new ChatInfoDto(
                "5f68ac64-ec83-4191-8fb7-da4588048766",
                null,
                null,
                null
        );
        ChatInfoDto chatInfoDto = chatService.chatInfo(chatId).getBody();
        assert chatInfoDto != null;
        assert Objects.equals(chatInfoDto, chatInfoDtoTest);
    }

    @Test
    public void viewChat() {
        String chatId = "d36580ba-5b4d-4a48-a84a-0362261a2060";
        UUID userId = UUID.fromString("540af0db-4862-40f1-8934-385f4da10ae1");

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = mock(Authentication.class);
        JwtUserData jwtUserData = new JwtUserData(userId,"gqwrhrehr", "Иванов Иван Иваныч");
        when(authentication.getPrincipal()).thenReturn(jwtUserData);
        context.setAuthentication(authentication);

        when(chatRepository.getByUuid(chatId))
                .thenReturn(Optional.of(chatEntity()));

        when(messageRepository.getAllByChatIdOrderBySentDateDesc(chatId))
                .thenReturn(List.of(messageEntity()));

        log.debug(chatService.viewChat(chatId).toString());
        log.debug(ResponseEntity.of(Optional.of(List.of(messageInfoDto()))).toString());
        assert Objects.equals(chatService.viewChat(chatId), ResponseEntity.of(Optional.of(List.of(messageInfoDto()))));
    }

    @SneakyThrows
    private ChatEntity chatEntity() {
        return new ChatEntity(
                "d36580ba-5b4d-4a48-a84a-0362261a2060",
                "540af0db-4862-40f1-8934-385f4da10ae1",
                "5f68ac64-ec83-4191-8fb7-da4588048766",
                ChatTypeEnum.DIALOGUE,
                "gwegwegewh",
                null,
                new Date(11111111),
                ""
        );
    }

    @SneakyThrows
    private MessageEntity messageEntity() {
        return new MessageEntity(
                "123",
                "d36580ba-5b4d-4a48-a84a-0362261a2060",
                "540af0db-4862-40f1-8934-385f4da10ae1",
                "erhrehreh",
                "gwegwegewh",
                new Date(123),
                "jrtjrtj",
                List.of(new AttachmentEntity())
        );
    }

    @SneakyThrows
    private MessageInfoDto messageInfoDto() {
        return new MessageInfoDto(
                "123",
                new Date(123),
                "jrtjrtj",
                "erhrehreh",
                "gwegwegewh",
                List.of(new FileIdNameSizeDto())
        );
    }

    @SneakyThrows
    private ChatUserEntity chatUserEntity() {
        return new ChatUserEntity(
                UUID.randomUUID().toString(),
                "d36580ba-5b4d-4a48-a84a-0362261a2060",
                "540af0db-4862-40f1-8934-385f4da10ae1"
        );
    }
}