package com.labs.java_lab1.chat.service;

import com.labs.java_lab1.chat.dto.SendFriendMessageDto;
import com.labs.java_lab1.chat.entity.AttachmentEntity;
import com.labs.java_lab1.chat.entity.ChatEntity;
import com.labs.java_lab1.chat.entity.ChatTypeEnum;
import com.labs.java_lab1.chat.entity.MessageEntity;
import com.labs.java_lab1.chat.repository.AttachmentRepository;
import com.labs.java_lab1.chat.repository.ChatRepository;
import com.labs.java_lab1.chat.repository.MessageRepository;
import com.labs.java_lab1.common.exception.RestTemplateErrorHandler;
import com.labs.java_lab1.common.exception.UserNotFoundException;
import com.labs.java_lab1.common.security.JwtUserData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final AttachmentRepository attachmentRepository;

    @Value("${app.security.integrations.api-key}")
    private String apiKey;

    @Value("${integration-urls.check-if-friend}")
    private String checkIfFriendUrl;

    public ResponseEntity<SendFriendMessageDto> sendFriendMessage(SendFriendMessageDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new RestTemplateErrorHandler());
        HashMap<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("friendId", dto.getFriendId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("API_KEY", apiKey);

        HttpEntity<HashMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(checkIfFriendUrl, request, String.class);

        if (!Objects.equals(response.getBody(), "true")) {
            log.error("User not found");
            throw new UserNotFoundException("User not found");
        }

        Optional<ChatEntity> optionalChat = chatRepository.getByUserIdAndFriendId(userId, dto.getFriendId());
        ChatEntity chat;
        if (optionalChat.isEmpty()) {
            chat = new ChatEntity(
                    UUID.randomUUID().toString(),
                    userId,
                    dto.getFriendId(),
                    ChatTypeEnum.DIALOGUE,
                    null,
                    null,
                    null,
                    null
            );
            chatRepository.save(chat);
        } else {
            chat = optionalChat.get();
        }

        MessageEntity messageEntity = new MessageEntity(
                UUID.randomUUID().toString(),
                chat.getUuid(),
                new Date(),
                dto.getText()
        );
        messageRepository.save(messageEntity);

        for (String attachmentId : dto.getAttachments()) {
            attachmentRepository.save(new AttachmentEntity(
                    UUID.randomUUID().toString(),
                    messageEntity.getUuid(),
                    attachmentId,
                    null
            ));
        }

        return ResponseEntity.ok(new SendFriendMessageDto(
                dto.getFriendId(),
                messageEntity.getMessage(),
                dto.getAttachments()
        ));
    }

}
