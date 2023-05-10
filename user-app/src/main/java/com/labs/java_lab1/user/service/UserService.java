package com.labs.java_lab1.user.service;

import com.labs.java_lab1.common.dto.NotifDto;
import com.labs.java_lab1.common.dto.NotifTypeEnum;
import com.labs.java_lab1.common.dto.UserMessageInfoDto;
import com.labs.java_lab1.common.dto.UserSyncDto;
import com.labs.java_lab1.common.exception.DateParseException;
import com.labs.java_lab1.common.exception.RestTemplateErrorHandler;
import com.labs.java_lab1.common.exception.UniqueConstraintViolationException;
import com.labs.java_lab1.common.exception.UserNotFoundException;
import com.labs.java_lab1.common.response.AuthenticationResponse;
import com.labs.java_lab1.common.security.JwtUserData;
import com.labs.java_lab1.user.dto.*;
import com.labs.java_lab1.user.entity.UserEntity;
import com.labs.java_lab1.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StreamBridge streamBridge;

    @Value("${app.security.integrations.api-key}")
    private String apiKey;

    @Value("${integration-urls.check-if-blacklisted}")
    private String checkIfBlacklisted;

    /**
     * Регистрация пользователя
     * @param dto данные пользователя
     * @return jwt токен
     */
    @Transactional
    public AuthenticationResponse save(CreateUserDto dto) {

        if (userRepository.findByLogin(dto.getLogin()).isPresent()) {
            log.error("User " + dto.getLogin() + " already exists");
            throw new UniqueConstraintViolationException("User " + dto.getLogin() + " already exists");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.error("Email " + dto.getEmail() + " is already used");
            throw new UniqueConstraintViolationException("Email " + dto.getEmail() + " is already used");
        }

        UserEntity entity = new UserEntity(
                UUID.randomUUID().toString(),
                dto.getLogin(),
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getFullName(),
                dto.getBirthDate(),
                dto.getPhoneNumber(),
                dto.getCity(),
                dto.getAvatar(),
                new Date()
        );

        AuthDto authDto = new AuthDto(
                entity.getLogin(),
                entity.getPassword()
        );

        userRepository.save(entity);
        log.info("User was added");
        String token = authenticationService.generateToken(authDto);
        log.info("Generated token");
        return new AuthenticationResponse(token);
    }

    /**
     * Вход пользователя в систему
     * @param dto дто с логином и паролем
     * @return jwt токен
     */
    @Transactional
    public AuthenticationResponse authenticate(AuthDto dto, HttpServletRequest request) {

        Optional<UserEntity> user = userRepository.findByLogin(dto.getLogin());

        userRepository.findByLogin(dto.getLogin())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(dto.getPassword(), user.get().getPassword())) {
            log.debug("Password " + user.get().getPassword() + " is incorrect");
            throw new UserNotFoundException("Incorrect password");
        }

        String token = authenticationService.generateToken(dto);
        log.info("Generated token");

        String notifString =
                "date=" + new Date() +
                ", ip=" + request.getRemoteAddr();
        NotifDto notifDto = new NotifDto(
                dto.getLogin(),
                NotifTypeEnum.LOG_IN,
                notifString
        );
        streamBridge.send("userNotifiedEvent-out-0", notifDto);
        return new AuthenticationResponse(token);
    }

    /**
     * Обновление данных профиля
     * @param dto дто с новыми данными
     * @return изменённые данные
     */
    @Transactional
    public UserDto update(UpdateUserDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String login = ((JwtUserData)authentication).getLogin();
        String userId = ((JwtUserData)authentication).getId().toString();

        if (userRepository.findByLogin(login).isEmpty()) {
            log.error("User " + login + " was not found");
            throw new UserNotFoundException("User " + login + " was not found");
        }

        UserEntity entity = userRepository.findByLogin(login).get();
        entity.setFullName(dto.getFullName());
        entity.setBirthDate(dto.getBirthDate());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setCity(dto.getCity());
        entity.setAvatar(dto.getAvatar());

        UserEntity createdEntity = userRepository.save(entity);
        log.info("User was updated");

        streamBridge.send("userModifiedEvent-out-0", new UserSyncDto(userId, dto.getFullName()));

        return new UserDto(
                createdEntity.getLogin(),
                createdEntity.getEmail(),
                createdEntity.getFullName(),
                createdEntity.getBirthDate(),
                createdEntity.getPhoneNumber(),
                createdEntity.getCity(),
                createdEntity.getAvatar()
        );
    }

    /**
     * Показ данных своего профиля
     * @return данные профиля
     */
    public UserDto getSelfProfile() {
        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((JwtUserData)authentication).getLogin();

        return getByLogin(username);
    }

    /**
     * Получение информации о пользователе по логину
     * @param login логин искомого пользователя
     * @return данные о пользователе
     */
    public UserDto getByLogin(String login) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((JwtUserData)authentication).getId().toString();

        if (userRepository.findByLogin(login).isEmpty()) {
            log.error("User " + login + " was not found");
            throw new UserNotFoundException("User " + login + " was not found");
        }

        UserEntity entity = userRepository.findByLogin(login).get();

        if (!Objects.equals(userId, entity.getUuid())) {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setErrorHandler(new RestTemplateErrorHandler());
            HashMap<String, String> map = new HashMap<>();
            map.put("userId", userId);
            map.put("friendId", entity.getUuid());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("API_KEY", apiKey);

            HttpEntity<HashMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(checkIfBlacklisted, request, String.class);

            if (Objects.equals(response.getBody(), "true")) {
                log.error("User blacklisted");
                throw new UserNotFoundException("User blacklisted");
            }
        }

        return new UserDto(
                entity.getLogin(),
                entity.getEmail(),
                entity.getFullName(),
                entity.getBirthDate(),
                entity.getPhoneNumber(),
                entity.getCity(),
                entity.getAvatar()
        );
    }

    /**
     * Проверка пользователя по id и имени
     * @param dto дто с id и именем
     * @return true - существует, false - нет
     */
    public boolean checkByIdAndName(UserFriendDto dto) {

        log.info("Checked by id and login");
        return userRepository.getByUuidAndFullName(dto.getFriendId(), dto.getFriendName()).isPresent();
    }

    /**
     * Проверка пользователя по id
     * @param dto дто с id
     * @return true - существует, false - нет
     */
    public boolean checkById(UserFriendIdDto dto) {

        log.info("Checked by id");
        return userRepository.getByUuid(dto.getFriendId()).isPresent();
    }

    /**
     * Получение пользователя по id
     * @param dto дто с id
     * @return данные о пользователе
     */
    public UserFriendDto getFriendById(UserFriendIdDto dto) {

        UserEntity entity = userRepository.getByUuid(dto.getFriendId()).get();
        log.info("Got user by id");

        return new UserFriendDto(
                entity.getUuid(),
                entity.getFullName()
        );
    }

    public UserMessageInfoDto getMessageInfoById(UserIdDto dto) {

        UserEntity entity = userRepository.getByUuid(dto.getUserId()).get();
        log.info("Got user by id");

        return new UserMessageInfoDto(
                entity.getFullName(),
                entity.getAvatar()
        );
    }

    /**
     * Получение списка пользователей по заданным параметрам
     * @param dto дто с фильртрами и пагинацией
     * @return список пользователей
     */
    @Transactional
    public List<UserDto> getFiltered(PaginationDto dto){

        Map<String, String> filters = dto.getFilters();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        if (filters.get("birthDate") != null) {
            try {
                date = formatter.parse(filters.get("birthDate"));
            } catch (ParseException e) {
                log.error(date + " is invalid date");
                throw new DateParseException("Invalid date format");
            }
        }

        Map<String, String> sortingMap = dto.getSorting();
        Sort sort = null;
        Sort fullSort = null;
        if (!sortingMap.isEmpty()) {
            for (String field : sortingMap.keySet()) {
                String order = sortingMap.get(field);
                sort = order.equalsIgnoreCase(Sort.Direction.ASC.name())
                        ? Sort.by(field).ascending()
                        : Sort.by(field).descending();
                if (fullSort == null) {
                    fullSort = sort;
                } else {
                    fullSort = fullSort.and(sort);
                }
            }
        }

        if (sort == null) {
            sort = Sort.by("fullName").ascending();
        }

        List<UserDto> entities = userRepository
                .findAllQuery(
                        filters.get("login"),
                        filters.get("fullName"),
                        date,
                        filters.get("phoneNumber"),
                        filters.get("city"),
                        PageRequest.of(dto.getPageNo() - 1, dto.getPageSize(), sort)
                );
        log.info("Found entities");
        return entities;
    }
}
