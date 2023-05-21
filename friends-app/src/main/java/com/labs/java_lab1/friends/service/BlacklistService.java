package com.labs.java_lab1.friends.service;

import com.labs.java_lab1.common.dto.NotifDto;
import com.labs.java_lab1.common.dto.NotifTypeEnum;
import com.labs.java_lab1.common.exception.DateParseException;
import com.labs.java_lab1.common.exception.RestTemplateErrorHandler;
import com.labs.java_lab1.common.exception.UniqueConstraintViolationException;
import com.labs.java_lab1.common.exception.UserNotFoundException;
import com.labs.java_lab1.common.security.JwtUserData;
import com.labs.java_lab1.friends.dto.*;
import com.labs.java_lab1.friends.entity.BlacklistEntity;
import com.labs.java_lab1.friends.exception.FriendAlreadyExistsException;
import com.labs.java_lab1.friends.exception.FriendNotFoundException;
import com.labs.java_lab1.friends.repository.BlacklistRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class BlacklistService {

    private final BlacklistRepository blacklistRepository;
    private final FriendsService friendsService;
    private final StreamBridge streamBridge;

    public BlacklistService(BlacklistRepository blacklistRepository,
                            @Lazy FriendsService friendsService,
                            StreamBridge streamBridge) {
        this.blacklistRepository = blacklistRepository;
        this.friendsService = friendsService;
        this.streamBridge = streamBridge;
    }

    @Value("${app.security.integrations.api-key}")
    private String apiKey;

    @Value("${integration-urls.check-id-name}")
    private String checkIdNameUrl;

    @Value("${integration-urls.check-id}")
    private String checkIdUrl;

    @Value("${integration-urls.sync}")
    private String syncUrl;

    /**
     * Получение списка пользователей в блэклисте
     * @param dto дто с фильтрацией и пагинацией
     * @return список пользователей
     */
    public List<GetFriendsDto> getBlacklist(PaginationDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        List<BlacklistEntity> entities = blacklistRepository
                .findAllByUserIdAndDeleteDateAndFriendNameContaining
                        (
                                userId,
                                null,
                                dto.getFriendName(),
                                PageRequest.of(dto.getPageNo() - 1, dto.getPageSize())
                        );

        log.info("Found entities");

        List<GetFriendsDto> dtos = new ArrayList<>();
        for (BlacklistEntity entity : entities) {
            dtos.add(new GetFriendsDto(
                    entity.getAddDate(),
                    null,
                    entity.getFriendId(),
                    entity.getFriendName()
            ));
        }
        return dtos;
    }

    /**
     * Добавление пользователя в блэклист
     * @param dto дто с данными о пользователе
     * @return данные о добавленном пользователе
     */
    @Transactional
    public AddFriendsDto save(AddFriendsIdDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JwtUserData data = (JwtUserData)authentication;

        if (Objects.equals(dto.getFriendId(), data.getId().toString())) {
            log.error("You can't add yourself");
            throw new UniqueConstraintViolationException("You can't add yourself");
        }

        if (friendsService.friendExists(dto.getFriendId())) {
            log.info("Friend exists");
            friendsService.deleteFriend(dto.getFriendId());
            log.info("Friend deleted");
        }

        Optional<BlacklistEntity> entityByUserFriend =
                blacklistRepository.getByUserIdAndFriendId(data.getId().toString(), dto.getFriendId());

        Optional<BlacklistEntity> entityByFriendUser =
                blacklistRepository.getByUserIdAndFriendId(dto.getFriendId(), data.getId().toString());

        if (entityByUserFriend.isPresent() && entityByUserFriend.get().getDeleteDate() != null) {
            log.info("Person was deleted from blacklist before");
            entityByUserFriend.get().setDeleteDate(null);
            entityByFriendUser.get().setDeleteDate(null);

            entityByUserFriend.get().setAddDate(new Date());
            entityByFriendUser.get().setAddDate(new Date());

            AddFriendsDto friendDto = syncPerson(dto.getFriendId());
            entityByUserFriend.get().setFriendName(friendDto.getFriendName());
            entityByFriendUser.get().setFriendName(data.getName());

            blacklistRepository.save(entityByUserFriend.get());
            blacklistRepository.save(entityByFriendUser.get());

            String notifString = "blacklistedId=" + data.getId();
            NotifDto notifDto = new NotifDto(
                    dto.getFriendId(),
                    NotifTypeEnum.NEW_BLACKLISTED,
                    notifString
            );
            streamBridge.send("userNotifiedEvent-out-0", notifDto);

            return new AddFriendsDto(
                    entityByUserFriend.get().getFriendId(),
                    entityByUserFriend.get().getFriendName()
            );
        } else if (entityByUserFriend.isPresent()) {
            log.error("Person already blacklisted");
            throw new FriendAlreadyExistsException("Person already blacklisted");
        }

        HttpServletRequest requestHeaders = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder
                .getRequestAttributes()))
                .getRequest();

        String token = requestHeaders.getHeader("Authorization");

        RestTemplate restTemplate = new RestTemplate();
        //restTemplate.setErrorHandler(new RestTemplateErrorHandler());
        HashMap<String, String> map = new HashMap<>();
        map.put("friendId", dto.getFriendId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("API_KEY", apiKey);
        headers.set("Authorization", token.substring(7));

        HttpEntity<HashMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(checkIdUrl, request, String.class);

        if (!Objects.equals(response.getBody(), "true")) {
            log.error("User not found");
            throw new UserNotFoundException("User not found");
        }

        HttpEntity<HashMap<String, String>> requestUserData = new HttpEntity<>(map, headers);
        ResponseEntity<AddFriendsDto> responseUserData = restTemplate
                .postForEntity(syncUrl, requestUserData, AddFriendsDto.class);

        BlacklistEntity entityFriend = new BlacklistEntity(
                UUID.randomUUID().toString(),
                new Date(),
                null,
                data.getId().toString(),
                responseUserData.getBody().getFriendId(),
                responseUserData.getBody().getFriendName()
        );

        BlacklistEntity entityUser = new BlacklistEntity(
                UUID.randomUUID().toString(),
                new Date(),
                null,
                dto.getFriendId(),
                data.getId().toString(),
                data.getName()
        );

        BlacklistEntity createdFriend = blacklistRepository.save(entityFriend);
        blacklistRepository.save(entityUser);
        log.info("Person was blacklisted");

        String notifString = "blacklistedId=" + data.getId();
        NotifDto notifDto = new NotifDto(
                dto.getFriendId(),
                NotifTypeEnum.NEW_BLACKLISTED,
                notifString
        );
        streamBridge.send("userNotifiedEvent-out-0", notifDto);

        return new AddFriendsDto(
                createdFriend.getFriendId(),
                createdFriend.getFriendName()
        );
    }

    /**
     * Получить данные о конкретном пользователе из блэклиста
     * @param friendId id этого пользователя
     * @return дто с данными об этом пользователе
     */
    public FriendDto getPerson(String friendId) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        Optional<BlacklistEntity> entityOptional = blacklistRepository.getByUserIdAndFriendId(userId, friendId);
        if (entityOptional.isEmpty() || entityOptional.get().getDeleteDate() != null) {
            log.error("Person not found");
            throw new FriendNotFoundException("Person not found");
        }

        BlacklistEntity entity = entityOptional.get();
        return new FriendDto(
                entity.getAddDate(),
                null,
                entity.getUserId(),
                entity.getFriendId(),
                entity.getFriendName()
        );
    }

    /**
     * Проверка на существование пользователя в блэклисте
     * @param friendId id проверяемого пользователя
     * @return true - существует, false - нет
     */
    public boolean personExists(String friendId) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        Optional<BlacklistEntity> entityOptional = blacklistRepository.getByUserIdAndFriendId(userId, friendId);
        if (entityOptional.isPresent() && entityOptional.get().getDeleteDate() == null) {
            log.info("Person exists");
            return true;
        } else {
            log.info("Person doesn't exist");
            return false;
        }
    }

    public boolean checkById(ChatFriendDto dto) {

        log.info("Checked by id");
        return blacklistRepository.getByUserIdAndFriendId(dto.getUserId(), dto.getFriendId()).isPresent();
    }

    /**
     * Синхронизация данных о конкретном пользователе в блэклисте
     * @param friendId id пользователя в блэклисте
     * @return дто с актульными данными об этом пользователе
     */
    @Transactional
    public AddFriendsDto syncPerson(String friendId) {

        if (blacklistRepository.getAllByFriendId(friendId).isEmpty()) {
            log.error("Person not found");
            throw new FriendNotFoundException("Person not found");
        }

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new RestTemplateErrorHandler());
        HashMap<String, String> map = new HashMap<>();
        map.put("friendId", friendId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("API_KEY", apiKey);

        HttpEntity<HashMap<String, String>> requestCheck = new HttpEntity<>(map, headers);
        ResponseEntity<String> responseCheck = restTemplate.postForEntity(checkIdUrl, requestCheck, String.class);

        if (!Objects.equals(responseCheck.getBody(), "true")) {
            log.error("User not found");
            throw new UserNotFoundException("User not found");
        }

        HttpEntity<HashMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<AddFriendsDto> response = restTemplate.postForEntity(syncUrl, request, AddFriendsDto.class);

        List<BlacklistEntity> entities = blacklistRepository.getAllByFriendId(friendId);
        for (BlacklistEntity entity : entities) {
            entity.setFriendId(Objects.requireNonNull(response.getBody()).getFriendId());
            entity.setFriendName(response.getBody().getFriendName());
            blacklistRepository.save(entity);
        }

        log.debug("Person " + Objects.requireNonNull(response.getBody()).getFriendName() + " was synced");
        return new AddFriendsDto(
                friendId,
                response.getBody().getFriendName()
        );
    }

    /**
     * Удаление пользователя из блэклиста
     * @param friendId id удаляемого пользователя
     * @return дто удалённого пользователя
     */
    @Transactional
    public ResponseEntity<DeleteFriendDto> deletePerson(String friendId) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        Optional<BlacklistEntity> entityFriend = blacklistRepository.getByUserIdAndFriendId(userId, friendId);
        if (entityFriend.isEmpty() || entityFriend.get().getDeleteDate() != null) {
            log.error("Person not found");
            throw new FriendNotFoundException("Person not found");
        }
        Optional<BlacklistEntity> entityUser = blacklistRepository.getByUserIdAndFriendId(friendId, userId);

        entityFriend.get().setDeleteDate(new Date());
        entityUser.get().setDeleteDate(new Date());

        blacklistRepository.save(entityFriend.get());
        blacklistRepository.save(entityUser.get());
        log.info("Person was deleted from blacklist");

        String notifString = "blacklistedId=" + userId;
        NotifDto notifDto = new NotifDto(
                friendId,
                NotifTypeEnum.DELETE_BLACKLISTED,
                notifString
        );
        streamBridge.send("userNotifiedEvent-out-0", notifDto);

        return ResponseEntity.ok(new DeleteFriendDto("Person successfully deleted"));
    }

    /**
     * Поиск пользователей в блэклисте по заданным параметрам
     * @param dto дто с пагинацией и фильтрами
     * @return список пользователей в блэклисте
     */
    public List<GetFriendsDto> searchBlacklist(SearchDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        Map<String, String> filters = dto.getFilters();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        if (filters.get("addDate") != null) {
            try {
                date = formatter.parse(filters.get("addDate"));
            } catch (ParseException e) {
                log.error(date + " is invalid date");
                throw new DateParseException("Invalid date format");
            }
        }

        BlacklistEntity example = BlacklistEntity
                .builder()
                .addDate(date)
                .deleteDate(null)
                .userId(userId)
                .friendId(filters.get("friendId"))
                .friendName(filters.get("friendName"))
                .build();

        Page<BlacklistEntity> entities =
                blacklistRepository.findAll(Example.of(example),
                        PageRequest.of(dto.getPageNo() - 1, dto.getPageSize()));
        log.info("Found entities");
        List<GetFriendsDto> dtos = new ArrayList<>();
        for (BlacklistEntity entity : entities) {
            if (entity.getDeleteDate() == null) {
                dtos.add(new GetFriendsDto(
                        entity.getAddDate(),
                        null,
                        entity.getFriendId(),
                        entity.getFriendName()
                ));
            }
        }
        return dtos;
    }
}
