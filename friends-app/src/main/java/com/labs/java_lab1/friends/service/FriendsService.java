package com.labs.java_lab1.friends.service;

import com.labs.java_lab1.common.dto.NotifDto;
import com.labs.java_lab1.common.dto.NotifTypeEnum;
import com.labs.java_lab1.common.exception.DateParseException;
import com.labs.java_lab1.common.exception.UniqueConstraintViolationException;
import com.labs.java_lab1.common.exception.UserNotFoundException;
import com.labs.java_lab1.common.security.JwtUserData;
import com.labs.java_lab1.friends.dto.*;
import com.labs.java_lab1.friends.entity.FriendsEntity;
import com.labs.java_lab1.friends.exception.FriendAlreadyExistsException;
import com.labs.java_lab1.friends.exception.FriendNotFoundException;
import com.labs.java_lab1.common.exception.RestTemplateErrorHandler;
import com.labs.java_lab1.friends.repository.FriendsRepository;
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
public class FriendsService {

    private final FriendsRepository friendsRepository;
    private final BlacklistService blacklistService;
    private final StreamBridge streamBridge;

    public FriendsService(FriendsRepository friendsRepository,
                          @Lazy BlacklistService blacklistService,
                          StreamBridge streamBridge) {
        this.friendsRepository = friendsRepository;
        this.blacklistService = blacklistService;
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

    public List<GetFriendsDto> getFriends(PagiantionDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        Page<FriendsEntity> entities =
                friendsRepository.findAllByUserIdAndDeleteDateAndFriendNameContaining
                        (userId, null, dto.getFriendName(),
                                PageRequest.of(dto.getPageNo() - 1, dto.getPageSize()));

        log.info("Found entities");

        List<GetFriendsDto> dtos = new ArrayList<>();
        for (FriendsEntity entity : entities) {
            dtos.add(new GetFriendsDto(
                    entity.getAddDate(),
                    null,
                    entity.getFriendId(),
                    entity.getFriendName()
            ));
        }
        return dtos;
    }

    @Transactional
    public AddFriendsDto save(AddFriendsIdDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JwtUserData data = (JwtUserData)authentication;

        if (Objects.equals(dto.getFriendId(), data.getId().toString())) {
            log.error("You can't add yourself");
            throw new UniqueConstraintViolationException("You can't add yourself");
        }

        if (blacklistService.personExists(dto.getFriendId())) {
            log.info("Person exists");
            blacklistService.deletePerson(dto.getFriendId());
            log.info("Person deleted");
        }

        Optional<FriendsEntity> entityByUserFriend =
                friendsRepository.getByUserIdAndFriendId(data.getId().toString(), dto.getFriendId());

        Optional<FriendsEntity> entityByFriendUser =
                friendsRepository.getByUserIdAndFriendId(dto.getFriendId(), data.getId().toString());

        if (entityByUserFriend.isPresent() && entityByUserFriend.get().getDeleteDate() != null) {
            log.info("Person was a friend before");
            entityByUserFriend.get().setDeleteDate(null);
            entityByFriendUser.get().setDeleteDate(null);

            entityByUserFriend.get().setAddDate(new Date());
            entityByFriendUser.get().setAddDate(new Date());

            AddFriendsDto friendDto = syncFriend(dto.getFriendId());
            entityByUserFriend.get().setFriendName(friendDto.getFriendName());
            entityByFriendUser.get().setFriendName(data.getName());

            friendsRepository.save(entityByUserFriend.get());
            friendsRepository.save(entityByFriendUser.get());

            String notifString = "friendId=" + data.getId();
            NotifDto notifDto = new NotifDto(
                    dto.getFriendId(),
                    NotifTypeEnum.NEW_FRIEND,
                    notifString
            );
            streamBridge.send("userNotifiedEvent-out-0", notifDto);

            return new AddFriendsDto(
                    entityByUserFriend.get().getFriendId(),
                    entityByUserFriend.get().getFriendName()
            );
        } else if (entityByUserFriend.isPresent()) {
            log.error("Friend is already added");
            throw new FriendAlreadyExistsException("Friend already added");
        }

        HttpServletRequest requestHeaders = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder
                .getRequestAttributes()))
                .getRequest();

        String token = requestHeaders.getHeader("Authorization");

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new RestTemplateErrorHandler());
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

        FriendsEntity entityFriend = new FriendsEntity(
                UUID.randomUUID().toString(),
                new Date(),
                null,
                data.getId().toString(),
                responseUserData.getBody().getFriendId(),
                responseUserData.getBody().getFriendName()
        );

        FriendsEntity entityUser = new FriendsEntity(
                UUID.randomUUID().toString(),
                new Date(),
                null,
                dto.getFriendId(),
                data.getId().toString(),
                data.getName()
        );

        FriendsEntity createdFriend = friendsRepository.save(entityFriend);
        friendsRepository.save(entityUser);
        log.info("Friend was added");

        String notifString = "friendId=" + data.getId();
        NotifDto notifDto = new NotifDto(
                dto.getFriendId(),
                NotifTypeEnum.NEW_FRIEND,
                notifString
        );
        streamBridge.send("userNotifiedEvent-out-0", notifDto);

        return new AddFriendsDto(
                createdFriend.getFriendId(),
                createdFriend.getFriendName()
        );
    }

    public FriendDto getFriend(String friendId) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        Optional<FriendsEntity> entityOptional = friendsRepository.getByUserIdAndFriendId(userId, friendId);
        if (entityOptional.isEmpty() || entityOptional.get().getDeleteDate() != null) {
            log.error("Friend not found");
            throw new FriendNotFoundException("Friend not found");
        }

        FriendsEntity entity = entityOptional.get();
        return new FriendDto(
                entity.getAddDate(),
                null,
                entity.getUserId(),
                entity.getFriendId(),
                entity.getFriendName()
        );
    }

    public boolean friendExists(String friendId) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        Optional<FriendsEntity> entityOptional = friendsRepository.getByUserIdAndFriendId(userId, friendId);
        if (entityOptional.isPresent() && entityOptional.get().getDeleteDate() == null) {
            log.info("Friend exists");
            return true;
        } else {
            log.info("Friend doesn't exist");
            return false;
        }
    }

    @Transactional
    public AddFriendsDto syncFriend(String friendId) {

        if (friendsRepository.getAllByFriendId(friendId).isEmpty()) {
            log.error("Friend not found");
            throw new FriendNotFoundException("Friend not found");
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

        List<FriendsEntity> entities = friendsRepository.getAllByFriendId(friendId);
        for (FriendsEntity entity : entities) {
            entity.setFriendId(Objects.requireNonNull(response.getBody()).getFriendId());
            entity.setFriendName(response.getBody().getFriendName());
            friendsRepository.save(entity);
        }

        log.debug("Friend " + Objects.requireNonNull(response.getBody()).getFriendName() + " was synced");
        return new AddFriendsDto(
                friendId,
                response.getBody().getFriendName()
        );
    }

    @Transactional
    public ResponseEntity<DeleteFriendDto> deleteFriend(String friendId) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        Optional<FriendsEntity> entityFriend = friendsRepository.getByUserIdAndFriendId(userId, friendId);
        if (entityFriend.isEmpty() || entityFriend.get().getDeleteDate() != null) {
            log.error("Friend not found");
            throw new FriendNotFoundException("Friend not found");
        }
        Optional<FriendsEntity> entityUser = friendsRepository.getByUserIdAndFriendId(friendId, userId);

        entityFriend.get().setDeleteDate(new Date());
        entityUser.get().setDeleteDate(new Date());

        friendsRepository.save(entityFriend.get());
        friendsRepository.save(entityUser.get());
        log.info("Friend was deleted");

        String notifString = "friendId=" + userId;
        NotifDto notifDto = new NotifDto(
                friendId,
                NotifTypeEnum.DELETE_FRIEND,
                notifString
        );
        streamBridge.send("userNotifiedEvent-out-0", notifDto);

        return ResponseEntity.ok(new DeleteFriendDto("Friend successfully deleted"));
    }

    public List<GetFriendsDto> searchFriends(SearchDto dto) {

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

        FriendsEntity example = FriendsEntity
                .builder()
                .addDate(date)
                .deleteDate(null)
                .userId(userId)
                .friendId(filters.get("friendId"))
                .friendName(filters.get("friendName"))
                .build();

        Page<FriendsEntity> entities =
                friendsRepository.findAll(Example.of(example),
                        PageRequest.of(dto.getPageNo() - 1, dto.getPageSize()));

        log.info("Found entities");
        List<GetFriendsDto> dtos = new ArrayList<>();
        for (FriendsEntity entity : entities) {
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

    public boolean checkById(ChatFriendDto dto) {

        log.info("Checked by id");
        return friendsRepository.getByUserIdAndFriendId(dto.getUserId(), dto.getFriendId()).isPresent();
    }
}