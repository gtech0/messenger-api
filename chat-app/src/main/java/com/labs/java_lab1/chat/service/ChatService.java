package com.labs.java_lab1.chat.service;

import com.labs.java_lab1.chat.dto.*;
import com.labs.java_lab1.chat.entity.*;
import com.labs.java_lab1.chat.exception.ChatNotFoundException;
import com.labs.java_lab1.chat.exception.ChatUserNotFoundException;
import com.labs.java_lab1.chat.repository.ChatRepository;
import com.labs.java_lab1.chat.repository.ChatUserRepository;
import com.labs.java_lab1.chat.repository.MessageRepository;
import com.labs.java_lab1.common.dto.FileIdNameSizeDto;
import com.labs.java_lab1.common.dto.NotifDto;
import com.labs.java_lab1.common.dto.NotifTypeEnum;
import com.labs.java_lab1.common.dto.UserMessageInfoDto;
import com.labs.java_lab1.common.exception.RestTemplateErrorHandler;
import com.labs.java_lab1.common.exception.UserNotFoundException;
import com.labs.java_lab1.common.security.JwtUserData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatUserRepository chatUserRepository;
    private final MessageRepository messageRepository;
    private final StreamBridge streamBridge;

    @Value("${app.security.integrations.api-key}")
    private String apiKey;

    @Value("${integration-urls.check-if-friend}")
    private String checkIfFriendUrl;

    @Value("${integration-urls.get-user-message-info}")
    private String getUserMessageInfoUrl;

    @Value("${integration-urls.get-file-info}")
    private String getFileInfoUrl;

    @Value("${integration-urls.download-attempt}")
    private String downloadAttemptUrl;

    /**
     * @param dto дто с текстом и получателем сообщения
     * @param files список прикреплённых файлов
     * @return входящее дто
     */
    @Transactional
    public ResponseEntity<SendChatMessageDto> sendFriendMessage(SendChatMessageDto dto,
                                                                List<MultipartFile> files) throws IOException {

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

        Optional<ChatEntity> optionalChat1 = chatRepository.getByUserIdAndFriendId(userId, dto.getReceiverId());
        Optional<ChatEntity> optionalChat2 = chatRepository.getByUserIdAndFriendId(dto.getReceiverId(), userId);
        ChatEntity chat;
        if (optionalChat1.isEmpty() && optionalChat2.isEmpty()) {
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

            chatUserRepository.save(new ChatUserEntity(
                    UUID.randomUUID().toString(),
                    chat.getUuid(),
                    userId
            ));

            chatUserRepository.save(new ChatUserEntity(
                    UUID.randomUUID().toString(),
                    chat.getUuid(),
                    dto.getReceiverId()
            ));

        } else {
            if (optionalChat1.isPresent()) {
                chat = optionalChat1.get();
            } else {
                chat = optionalChat2.get();
            }
        }

        map = new HashMap<>();
        map.put("userId", userId);

        HttpEntity<HashMap<String, String>> userDataRequest = new HttpEntity<>(map, headers);
        ResponseEntity<UserMessageInfoDto> userDataResponse =
                restTemplate.postForEntity(getUserMessageInfoUrl, userDataRequest, UserMessageInfoDto.class);

        List<AttachmentEntity> attachmentEntities = new ArrayList<>();
        MessageEntity messageEntity = new MessageEntity(
                UUID.randomUUID().toString(),
                chat.getUuid(),
                userId,
                Objects.requireNonNull(userDataResponse.getBody()).getFullName(),
                userDataResponse.getBody().getAvatar(),
                new Date(),
                dto.getText(),
                null
        );

        restTemplate = new RestTemplate();
        //restTemplate.setErrorHandler(new RestTemplateErrorHandler());

        for (MultipartFile file : files) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("fileName", file.getOriginalFilename());
            hashMap.put("bytes", file.getBytes());
            HttpEntity<HashMap<String, Object>> fileRequest = new HttpEntity<>(hashMap, headers);

            ResponseEntity<FileIdNameSizeDto> fileResponse = restTemplate
                    .postForEntity(getFileInfoUrl, fileRequest, FileIdNameSizeDto.class);
            log.debug(Objects.requireNonNull(fileResponse.getBody()).getFileName());

            AttachmentEntity attachmentEntity = new AttachmentEntity(
                    UUID.randomUUID().toString(),
                    fileResponse.getBody().getId(),
                    fileResponse.getBody().getFileName(),
                    fileResponse.getBody().getSize(),
                    messageEntity
            );
            attachmentEntities.add(attachmentEntity);
        }

        messageEntity.setAttachments(attachmentEntities);
        messageRepository.save(messageEntity);

        return ResponseEntity.ok(new SendChatMessageDto(
                dto.getReceiverId(),
                messageEntity.getMessage()
        ));
    }

    /**
     * Создание чата
     * @param dto дто с названием, аватаром и начальными участниками чата
     * @return то же дто, но ещё с текущим юзером
     */
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
            checkForFriends(userId, restTemplate, headers, friendId);
        }

        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new RestTemplateErrorHandler());

        HttpEntity<Void> httpHeaders = new HttpEntity<>(headers);

        ResponseEntity<Boolean> fileResponse = restTemplate
                .exchange(downloadAttemptUrl + dto.getAvatar(), HttpMethod.GET, httpHeaders, Boolean.class);

        if (!Objects.equals(fileResponse.getBody(), true)) {
            log.error("File not found");
            throw new UserNotFoundException("File " + dto.getAvatar() + " not found");
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

    /**
     * Изменение чата
     * @param dto данные чата, включая список пользователей
     *            можно удалять только своих друзей, если ты не админ
     * @return входящее дто
     */
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



        List<String> chatUserList = chatUserRepository.getAllByChatIdQuery(dto.getId());
        List<String> addedUsers = new ArrayList<>();
        List<String> removedUsers = new ArrayList<>();

        for (String currentUser : chatUserList) {
            if (!dto.getUsers().contains(currentUser) && !Objects.equals(currentUser, chat.get().getAdminId())) {
                removedUsers.add(currentUser);
            }
        }

        for (String newUser : dto.getUsers()) {
            if (!chatUserList.contains(newUser)) {
                addedUsers.add(newUser);
            }
        }
        checkForFriendsFor(userId, restTemplate, headers, addedUsers);

        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new RestTemplateErrorHandler());

        HttpEntity<Void> httpHeaders = new HttpEntity<>(headers);

        ResponseEntity<Boolean> fileResponse = restTemplate
                .exchange(downloadAttemptUrl + dto.getAvatar(), HttpMethod.GET, httpHeaders, Boolean.class);

        if (!Objects.equals(fileResponse.getBody(), true)) {
            log.error("File not found");
            throw new UserNotFoundException("File " + dto.getAvatar() + " not found");
        }

        for (String friendId : addedUsers) {
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

        if (!Objects.equals(chat.get().getAdminId(), userId)) {
            checkForFriendsFor(userId, restTemplate, headers, removedUsers);
        }

        chatUserRepository.deleteAllByChatIdAndUserIdIn(dto.getId(), removedUsers);

        chat.get().setName(dto.getName());
        chat.get().setAvatar(dto.getAvatar());
        chatRepository.save(chat.get());

        return ResponseEntity.ok(new UpdateChatDto(
                chat.get().getUuid(),
                chat.get().getName(),
                chat.get().getAvatar(),
                chatUserRepository.getAllByChatIdQuery(dto.getId())
        ));
    }

    private void checkForFriendsFor(String userId, RestTemplate restTemplate, HttpHeaders headers, List<String> userList) {
        for (String friendId : userList) {
            checkForFriends(userId, restTemplate, headers, friendId);
        }
    }

    private void checkForFriends(String userId, RestTemplate restTemplate, HttpHeaders headers, String friendId) {
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

    /**
     * Отправление сообщения в чат/диалог
     * @param dto дто с текстом и получателем сообщения
     * @param files прикреплённые в сообщению файлы
     * @return входящее дто
     */
    @Transactional
    public ResponseEntity<SendChatMessageDto> sendChatMessage(SendChatMessageDto dto,
                                                              List<MultipartFile> files) throws IOException {

        Optional<ChatEntity> chat = chatRepository.getByUuid(dto.getReceiverId());
        if (chat.isEmpty()) {
            throw new ChatNotFoundException("Chat not found");
        }

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();
        String login = ((JwtUserData)authentication).getLogin();

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

        List<AttachmentEntity> attachmentEntities = new ArrayList<>();
        MessageEntity messageEntity = new MessageEntity(
                UUID.randomUUID().toString(),
                chat.get().getUuid(),
                userId,
                Objects.requireNonNull(userDataResponse.getBody()).getFullName(),
                userDataResponse.getBody().getAvatar(),
                new Date(),
                dto.getText(),
                null
        );

        restTemplate = new RestTemplate();
        //restTemplate.setErrorHandler(new RestTemplateErrorHandler());

        for (MultipartFile file : files) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("fileName", file.getOriginalFilename());
            hashMap.put("bytes", file.getBytes());
            HttpEntity<HashMap<String, Object>> fileRequest = new HttpEntity<>(hashMap, headers);

            ResponseEntity<FileIdNameSizeDto> fileResponse = restTemplate
                    .postForEntity(getFileInfoUrl, fileRequest, FileIdNameSizeDto.class);
            log.debug(Objects.requireNonNull(fileResponse.getBody()).getFileName());

            AttachmentEntity attachmentEntity = new AttachmentEntity(
                    UUID.randomUUID().toString(),
                    fileResponse.getBody().getId(),
                    fileResponse.getBody().getFileName(),
                    fileResponse.getBody().getSize(),
                    messageEntity
            );
            attachmentEntities.add(attachmentEntity);
        }

        messageEntity.setAttachments(attachmentEntities);
        messageRepository.save(messageEntity);

        if (chat.get().getType() == ChatTypeEnum.DIALOGUE) {
            String notifString =
                    "senderId=" + userId +
                    ", date=" + new Date() +
                    ", message=" + messageEntity
                            .getMessage()
                            .substring(0, Math.min(messageEntity.getMessage().length(), 100));

            NotifDto notifDto = new NotifDto(
                    login,
                    NotifTypeEnum.NEW_MESSAGE,
                    notifString
            );
            streamBridge.send("userNotifiedEvent-out-0", notifDto);
        }

        return ResponseEntity.ok(new SendChatMessageDto(
                dto.getReceiverId(),
                messageEntity.getMessage()
        ));
    }

    /**
     * Вывод основной информации о чате/диалоге
     * @param id идентификатор чата
     * @return дто с информацией о чате
     */
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

    /**
     * Просмотр чата
     * @param id идентификатор чата
     * @return список сообщений чата
     */
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
        List<MessageEntity> messageEntities = messageRepository.getAllByChatIdOrderBySentDateDesc(id);

        List<MessageInfoDto> messageInfoDtoList = new ArrayList<>();
        for (MessageEntity entity : messageEntities) {
            List<FileIdNameSizeDto> attachmentNames = new ArrayList<>();
            for (AttachmentEntity attachment : entity.getAttachments()) {
                attachmentNames.add(new FileIdNameSizeDto(
                        attachment.getFileId(),
                        attachment.getFileName(),
                        attachment.getFileSize()
                ));
            }

            messageInfoDtoList.add(new MessageInfoDto(
                    entity.getUuid(),
                    entity.getSentDate(),
                    entity.getMessage(),
                    entity.getFullName(),
                    entity.getAvatar(),
                    attachmentNames
            ));
        }
        return ResponseEntity.ok(messageInfoDtoList);
    }

    /**
     * Просмотр списка чатов
     * @param dto дто с пагинацией и фильтром по названию чата
     * @return список последних сообщений чатов
     */
    public ResponseEntity<List<ChatListDto>> viewChatList(ChatListPaginationDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        if (dto.getPageNo() == null) {
            dto.setPageNo(1);
        }

        if (dto.getPageSize() == null) {
            dto.setPageSize(50);
        }

        List<ChatUserEntity> chatUserEntityList = chatUserRepository
                .getAllByUserId(userId, PageRequest.of(dto.getPageNo() - 1, dto.getPageSize()));

        List<ChatEntity> chatEntities = new ArrayList<>();
        for (ChatUserEntity chatUserEntity : chatUserEntityList) {
            Optional<ChatEntity> chat;
            if (dto.getName() == null) {
                chat = chatRepository.getByUuid(chatUserEntity.getChatId());
            } else {
                chat = chatRepository.getByUuidAndNameContaining(chatUserEntity.getChatId(), dto.getName());
            }
            chat.ifPresent(chatEntities::add);
        }

        List<ChatListDto> chatListDtoList = new ArrayList<>();
        for (ChatEntity chatEntity : chatEntities) {
            Optional<MessageEntity> firstMessage = messageRepository
                    .getFirstByChatIdOrderBySentDateDesc(chatEntity.getUuid());

            if (firstMessage.isPresent()) {
                boolean attachmentsPresent = !firstMessage.get().getAttachments().isEmpty();

                if (chatEntity.getType() == ChatTypeEnum.CHAT) {
                    chatListDtoList.add(new ChatListDto(
                            chatEntity.getUuid(),
                            chatEntity.getName(),
                            firstMessage.get().getMessage(),
                            attachmentsPresent,
                            firstMessage.get().getSentDate(),
                            firstMessage.get().getUserId()
                    ));
                } else if (chatEntity.getType() == ChatTypeEnum.DIALOGUE) {
                    if (!Objects.equals(firstMessage.get().getUserId(), userId)) {
                        chatListDtoList.add(new ChatListDto(
                                chatEntity.getUuid(),
                                firstMessage.get().getFullName(),
                                firstMessage.get().getMessage(),
                                attachmentsPresent,
                                firstMessage.get().getSentDate(),
                                firstMessage.get().getUserId()
                        ));
                    } else {
                        RestTemplate restTemplate = new RestTemplate();
                        //restTemplate.setErrorHandler(new RestTemplateErrorHandler());
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.set("API_KEY", apiKey);

                        HashMap<String, String> map = new HashMap<>();
                        map.put("userId", chatEntity.getFriendId());

                        HttpEntity<HashMap<String, String>> userDataRequest = new HttpEntity<>(map, headers);

                        ResponseEntity<UserMessageInfoDto> userDataResponse = restTemplate.
                                postForEntity(getUserMessageInfoUrl, userDataRequest, UserMessageInfoDto.class);

                        chatListDtoList.add(new ChatListDto(
                                chatEntity.getUuid(),
                                Objects.requireNonNull(userDataResponse.getBody()).getFullName(),
                                firstMessage.get().getMessage(),
                                attachmentsPresent,
                                firstMessage.get().getSentDate(),
                                firstMessage.get().getUserId()
                        ));
                    }
                }
            }
        }
        return ResponseEntity.ok(chatListDtoList);
    }

    /**
     * Поиск по соощениям чата
     * @param dto дто с искомым текстом сообщения
     * @return список сообщений
     */
    public ResponseEntity<List<MessageSearchDto>> messageSearch(MessageSearchBodyDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        List<ChatUserEntity> chatUserEntityList = chatUserRepository.getAllByUserId(userId);
        List<ChatEntity> chatEntities = new ArrayList<>();
        for (ChatUserEntity chatUserEntity : chatUserEntityList) {
            Optional<ChatEntity> chat = chatRepository.getByUuid(chatUserEntity.getChatId());

            chat.ifPresent(chatEntities::add);
        }

        List<MessageSearchDto> messageSearchDtoList = new ArrayList<>();
        for (ChatEntity chatEntity : chatEntities) {
            List<QueryMessageSearchDto> chatMessages = messageRepository
                    .getAllByChatIdAndMessageLike(userId, chatEntity.getUuid(), dto.getText());

            for (QueryMessageSearchDto messageEntity : chatMessages) {
                Optional<MessageEntity> optionalMessage = messageRepository
                        .getByUuid(messageEntity.getMessageId());

                List<String> attachmentNames = new ArrayList<>();
                if (optionalMessage.isPresent()) {
                    for (AttachmentEntity attachment : optionalMessage.get().getAttachments()) {
                        attachmentNames.add(attachment.getFileName());
                    }
                }

                boolean attachmentsPresent = !attachmentNames.isEmpty();

                if (chatEntity.getType() == ChatTypeEnum.CHAT) {
                    messageSearchDtoList.add(new MessageSearchDto(
                            chatEntity.getUuid(),
                            chatEntity.getName(),
                            messageEntity.getMessage(),
                            attachmentsPresent,
                            messageEntity.getSentDate(),
                            attachmentNames
                    ));
                } else if (chatEntity.getType() == ChatTypeEnum.DIALOGUE) {
                    if (!Objects.equals(messageEntity.getUserId(), userId)) {
                        messageSearchDtoList.add(new MessageSearchDto(
                                chatEntity.getUuid(),
                                messageEntity.getFullName(),
                                messageEntity.getMessage(),
                                attachmentsPresent,
                                messageEntity.getSentDate(),
                                attachmentNames
                        ));
                    } else {
                        RestTemplate restTemplate = new RestTemplate();
                        //restTemplate.setErrorHandler(new RestTemplateErrorHandler());
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.set("API_KEY", apiKey);

                        HashMap<String, String> map = new HashMap<>();
                        map.put("userId", chatEntity.getFriendId());

                        HttpEntity<HashMap<String, String>> userDataRequest = new HttpEntity<>(map, headers);

                        ResponseEntity<UserMessageInfoDto> userDataResponse = restTemplate.
                                postForEntity(getUserMessageInfoUrl, userDataRequest, UserMessageInfoDto.class);

                        messageSearchDtoList.add(new MessageSearchDto(
                                chatEntity.getUuid(),
                                Objects.requireNonNull(userDataResponse.getBody()).getFullName(),
                                messageEntity.getMessage(),
                                attachmentsPresent,
                                messageEntity.getSentDate(),
                                attachmentNames
                        ));
                    }
                }
            }
        }
        messageSearchDtoList.sort(Comparator.comparing(MessageSearchDto::getSentDate));
        return ResponseEntity.ok(messageSearchDtoList);
    }
}
