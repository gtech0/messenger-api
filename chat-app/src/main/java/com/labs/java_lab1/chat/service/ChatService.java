package com.labs.java_lab1.chat.service;

import com.labs.java_lab1.chat.dto.*;
import com.labs.java_lab1.chat.entity.*;
import com.labs.java_lab1.chat.exception.ChatNotFoundException;
import com.labs.java_lab1.chat.exception.ChatUserNotFoundException;
import com.labs.java_lab1.chat.repository.AttachmentRepository;
import com.labs.java_lab1.chat.repository.ChatRepository;
import com.labs.java_lab1.chat.repository.ChatUserRepository;
import com.labs.java_lab1.chat.repository.MessageRepository;
import com.labs.java_lab1.common.dto.UserMessageInfoDto;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatUserRepository chatUserRepository;
    private final MessageRepository messageRepository;
    private final AttachmentRepository attachmentRepository;

    @Value("${app.security.integrations.api-key}")
    private String apiKey;

    @Value("${integration-urls.check-if-friend}")
    private String checkIfFriendUrl;

    @Value("${integration-urls.get-user-message-info}")
    private String getUserMessageInfoUrl;

    @Transactional
    public ResponseEntity<SendMessageDto> sendFriendMessage(SendMessageDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        RestTemplate restTemplate = new RestTemplate();
        //restTemplate.setErrorHandler(new RestTemplateErrorHandler());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("API_KEY", apiKey);

        HashMap<String, String> map = new HashMap<>();
        map.put("userId", userId);
        map.put("friendId", dto.getReceiverId());

        HttpEntity<HashMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(checkIfFriendUrl, request, String.class);

        if (!Objects.equals(response.getBody(), "true")) {
            log.error("Friend not found");
            throw new UserNotFoundException("Friend not found");
        }

        Optional<ChatEntity> optionalChat = chatRepository.getByUserIdAndFriendId(userId, dto.getReceiverId());
        ChatEntity chat;
        if (optionalChat.isEmpty()) {
            chat = new ChatEntity(
                    UUID.randomUUID().toString(),
                    userId,
                    dto.getReceiverId(),
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

        map = new HashMap<>();
        map.put("userId", userId);

        HttpEntity<HashMap<String, String>> userDataRequest = new HttpEntity<>(map, headers);
        ResponseEntity<UserMessageInfoDto> userDataResponse =
                restTemplate.postForEntity(getUserMessageInfoUrl, userDataRequest, UserMessageInfoDto.class);

        MessageEntity messageEntity = new MessageEntity(
                UUID.randomUUID().toString(),
                chat.getUuid(),
                userId,
                userDataResponse.getBody().getFullName(),
                userDataResponse.getBody().getAvatar(),
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

        return ResponseEntity.ok(new SendMessageDto(
                dto.getReceiverId(),
                messageEntity.getMessage(),
                dto.getAttachments()
        ));
    }

    @Transactional
    public ResponseEntity<CreateChatDto> createChat(CreateChatDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new RestTemplateErrorHandler());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("API_KEY", apiKey);

        for (String friendId : dto.getUsers()) {
            HashMap<String, String> map = new HashMap<>();
            map.put("userId", userId);
            map.put("friendId", friendId);

            HttpEntity<HashMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(checkIfFriendUrl, request, String.class);

            if (!Objects.equals(response.getBody(), "true")) {
                log.error("Friend not found");
                throw new UserNotFoundException("Friend " + friendId + " not found");
            }
        }

        dto.getUsers().add(userId);
        List<String> userList = dto.getUsers();

        ChatEntity chat = new ChatEntity(
                UUID.randomUUID().toString(),
                null,
                null,
                ChatTypeEnum.CHAT,
                dto.getName(),
                userId,
                new Date(),
                dto.getAvatar()
        );
        chatRepository.save(chat);

        for (String friendId : dto.getUsers()) {
            ChatUserEntity userEntity = new ChatUserEntity(
                    UUID.randomUUID().toString(),
                    chat.getUuid(),
                    friendId
            );
            chatUserRepository.save(userEntity);
        }

        return ResponseEntity.ok(new CreateChatDto(
                chat.getName(),
                chat.getAvatar(),
                userList
        ));
    }

    @Transactional
    public ResponseEntity<UpdateChatDto> updateChat(UpdateChatDto dto) {

        Optional<ChatEntity> chat = chatRepository.getByUuid(dto.getId());
        if (chat.isEmpty()) {
            throw new ChatNotFoundException("Chat not found");
        }

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new RestTemplateErrorHandler());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("API_KEY", apiKey);

        for (String friendId : dto.getUsers()) {
            HashMap<String, String> map = new HashMap<>();
            map.put("userId", userId);
            map.put("friendId", friendId);

            HttpEntity<HashMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(checkIfFriendUrl, request, String.class);

            if (!Objects.equals(response.getBody(), "true")) {
                log.error("Friend not found");
                throw new UserNotFoundException("Friend " + friendId + " not found");
            }
        }

        for (String friendId : dto.getUsers()) {
            Optional<ChatUserEntity> optionalChatUser =
                    chatUserRepository.getByChatIdAndUserId(dto.getId(), friendId);

            if (optionalChatUser.isEmpty()) {
                ChatUserEntity userEntity = new ChatUserEntity(
                        UUID.randomUUID().toString(),
                        dto.getId(),
                        friendId
                );
                chatUserRepository.save(userEntity);
            }
        }
        dto.getUsers().add(userId);
        chatUserRepository.deleteAllByChatIdAndUserIdNotIn(dto.getId(), dto.getUsers());

        chat.get().setName(dto.getName());
        chat.get().setAvatar(dto.getAvatar());
        chatRepository.save(chat.get());

        return ResponseEntity.ok(new UpdateChatDto(
                chat.get().getUuid(),
                chat.get().getName(),
                chat.get().getAvatar(),
                dto.getUsers()
        ));
    }

    @Transactional
    public ResponseEntity<SendMessageDto> sendChatMessage(SendMessageDto dto) {

        Optional<ChatEntity> chat = chatRepository.getByUuid(dto.getReceiverId());
        if (chat.isEmpty()) {
            throw new ChatNotFoundException("Chat not found");
        }

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        Optional<ChatUserEntity> optionalChatUser =
                chatUserRepository.getByChatIdAndUserId(dto.getReceiverId(), userId);

        if (optionalChatUser.isEmpty()) {
            throw new ChatUserNotFoundException("This user is not in this chat");
        }

        RestTemplate restTemplate = new RestTemplate();
        //restTemplate.setErrorHandler(new RestTemplateErrorHandler());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("API_KEY", apiKey);

        HashMap<String, String> map = new HashMap<>();
        map.put("userId", userId);

        HttpEntity<HashMap<String, String>> userDataRequest = new HttpEntity<>(map, headers);
        ResponseEntity<UserMessageInfoDto> userDataResponse =
                restTemplate.postForEntity(getUserMessageInfoUrl, userDataRequest, UserMessageInfoDto.class);

        MessageEntity messageEntity = new MessageEntity(
                UUID.randomUUID().toString(),
                chat.get().getUuid(),
                userId,
                userDataResponse.getBody().getFullName(),
                userDataResponse.getBody().getAvatar(),
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

        return ResponseEntity.ok(new SendMessageDto(
                dto.getReceiverId(),
                messageEntity.getMessage(),
                dto.getAttachments()
        ));
    }

    public ResponseEntity<ChatInfoDto> chatInfo(String id) {

        Optional<ChatEntity> chat = chatRepository.getByUuid(id);
        if (chat.isEmpty()) {
            throw new ChatNotFoundException("Chat not found");
        }

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        if (chat.get().getType() == ChatTypeEnum.CHAT) {

            Optional<ChatUserEntity> optionalChatUser =
                    chatUserRepository.getByChatIdAndUserId(id, userId);

            if (optionalChatUser.isEmpty()) {
                throw new ChatUserNotFoundException("This user is not in this chat");
            }

            return ResponseEntity.ok(new ChatInfoDto(
                    chat.get().getName(),
                    chat.get().getAvatar(),
                    chat.get().getAdminId(),
                    chat.get().getCreationDate()
            ));

        } else if (chat.get().getType() == ChatTypeEnum.DIALOGUE) {

            if (Objects.equals(chat.get().getUserId(), userId) || Objects.equals(chat.get().getFriendId(), userId)) {

                return ResponseEntity.ok(new ChatInfoDto(
                        chat.get().getFriendId(),
                        null,
                        null,
                        null
                ));

            } else {
                throw new ChatUserNotFoundException("This user is not in this chat");
            }

        } else {
            throw new RuntimeException("This chat type doesn't exist");
        }
    }

    public ResponseEntity<List<MessageInfoDto>> viewChat(String id) {

        Optional<ChatEntity> chat = chatRepository.getByUuid(id);
        if (chat.isEmpty()) {
            throw new ChatNotFoundException("Chat not found");
        }

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        if (chat.get().getType() == ChatTypeEnum.CHAT) {
            Optional<ChatUserEntity> optionalChatUser =
                    chatUserRepository.getByChatIdAndUserId(id, userId);

            if (optionalChatUser.isEmpty()) {
                throw new ChatUserNotFoundException("This user is not in this chat");
            }
            return getMessageList(id);

        } else if (chat.get().getType() == ChatTypeEnum.DIALOGUE) {
            if (Objects.equals(chat.get().getUserId(), userId) || Objects.equals(chat.get().getFriendId(), userId)) {
                return getMessageList(id);
            } else {
                throw new ChatUserNotFoundException("This user is not in this chat");
            }
        } else {
            throw new RuntimeException("This chat type doesn't exist");
        }
    }

    private ResponseEntity<List<MessageInfoDto>> getMessageList(String id) {
        List<MessageEntity> messageEntities =
                messageRepository.getAllByChatIdOrderBySentDateAsc(id);

        List<MessageInfoDto> messageInfoDtoList = new ArrayList<>();
        for (MessageEntity entity : messageEntities) {
            messageInfoDtoList.add(new MessageInfoDto(
                    entity.getUuid(),
                    entity.getSentDate(),
                    entity.getMessage(),
                    entity.getFullName(),
                    entity.getAvatar()
            ));
        }
        return ResponseEntity.ok(messageInfoDtoList);
    }
}
