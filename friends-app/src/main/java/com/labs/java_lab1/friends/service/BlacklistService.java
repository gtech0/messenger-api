package com.labs.java_lab1.friends.service;

import com.labs.java_lab1.common.exception.DateParseException;
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

    public BlacklistService(BlacklistRepository blacklistRepository, @Lazy FriendsService friendsService) {
        this.blacklistRepository = blacklistRepository;
        this.friendsService = friendsService;
    }

    @Value("${app.security.integrations.api-key}")
    private String apiKey;

    public List<GetFriendsDto> getBlacklist(PagiantionDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        Page<BlacklistEntity> entities =
                blacklistRepository.findAllByUserIdAndFriendNameContaining
                        (userId, dto.getFriendName(), PageRequest.of(dto.getPageNo() - 1, dto.getPageSize()));

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

    public AddFriendsDto save(AddFriendsDto dto) {

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
        String url = "http://localhost:8010/integration/users/checkidname";

        HashMap<String, String> map = new HashMap<>();
        map.put("friendId", dto.getFriendId());
        map.put("friendName", dto.getFriendName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("API_KEY", apiKey);
        headers.set("Authorization", token.substring(7));

        HttpEntity<HashMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (!Objects.equals(response.getBody(), "true")) {
            log.error("User not found");
            throw new UserNotFoundException("User not found");
        }

        BlacklistEntity entityFriend = new BlacklistEntity(
                UUID.randomUUID().toString(),
                new Date(),
                null,
                data.getId().toString(),
                dto.getFriendId(),
                dto.getFriendName()
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
        return new AddFriendsDto(
                createdFriend.getFriendId(),
                createdFriend.getFriendName()
        );
    }

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

    public AddFriendsDto syncPerson(String friendId) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        if (blacklistRepository.getByUserIdAndFriendId(userId, friendId).isEmpty()) {
            log.error("Person not found");
            throw new FriendNotFoundException("Person not found");
        }

        HttpServletRequest requestHeaders = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder
                .getRequestAttributes()))
                .getRequest();

        String token = requestHeaders.getHeader("Authorization");

        RestTemplate restTemplate = new RestTemplate();
        String urlCheck = "http://localhost:8010/integration/users/checkid";

        HashMap<String, String> map = new HashMap<>();
        map.put("friendId", friendId);
        map.put("friendName", "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("API_KEY", apiKey);
        headers.set("Authorization", token.substring(7));

        HttpEntity<HashMap<String, String>> requestCheck = new HttpEntity<>(map, headers);

        ResponseEntity<String> responseCheck = restTemplate.postForEntity(urlCheck, requestCheck, String.class);

        if (!Objects.equals(responseCheck.getBody(), "true")) {
            log.error("User not found");
            throw new UserNotFoundException("User not found");
        }

        String url = "http://localhost:8010/integration/users/sync";

        HttpEntity<HashMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<AddFriendsDto> response = restTemplate.postForEntity(url, request, AddFriendsDto.class);

        BlacklistEntity entity = blacklistRepository.getByUserIdAndFriendId(userId, friendId).get();
        entity.setFriendId(Objects.requireNonNull(response.getBody()).getFriendId());
        entity.setFriendName(response.getBody().getFriendName());
        blacklistRepository.save(entity);
        log.debug("Person " + entity.getFriendName() + " was synced");
        return new AddFriendsDto(
                entity.getFriendId(),
                entity.getFriendName()
        );
    }

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
        return ResponseEntity.ok(new DeleteFriendDto("Person successfully deleted"));
    }

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
