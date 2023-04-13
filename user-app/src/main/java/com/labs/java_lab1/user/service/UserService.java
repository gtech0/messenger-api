package com.labs.java_lab1.user.service;

import com.labs.java_lab1.user.dto.UserFriendDto;
import com.labs.java_lab1.user.dto.*;
import com.labs.java_lab1.user.entity.UserEntity;
import com.labs.java_lab1.user.exception.DateParseException;
import com.labs.java_lab1.common.exception.UniqueConstraintViolationException;
import com.labs.java_lab1.common.exception.UserNotFoundException;
import com.labs.java_lab1.user.repository.UserRepository;
import com.labs.java_lab1.common.security.JwtUserData;
import com.labs.java_lab1.common.response.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public AuthenticationResponse save(CreateUserDto dto) {

        if (userRepository.findByLogin(dto.getLogin()).isPresent()) {
            throw new UniqueConstraintViolationException("User " + dto.getLogin() + " already exists");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
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
        String token = authenticationService.generateToken(authDto);
        return new AuthenticationResponse(token);
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthDto dto) {

        Optional<UserEntity> user = userRepository.findByLogin(dto.getLogin());

        userRepository.findByLogin(dto.getLogin())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(dto.getPassword(), user.get().getPassword())) {
            throw new UserNotFoundException("Incorrect password");
        }

        String token = authenticationService.generateToken(dto);
        return new AuthenticationResponse(token);
    }

    @Transactional
    public UserDto update(UpdateUserDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String login = ((JwtUserData)authentication).getLogin();

        if (userRepository.findByLogin(login).isEmpty()) {
            throw new UserNotFoundException("User " + login + " was not found");
        }

        UserEntity entity = userRepository.findByLogin(login).get();
        entity.setFullName(dto.getFullName());
        entity.setBirthDate(dto.getBirthDate());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setCity(dto.getCity());
        entity.setAvatar(dto.getAvatar());

        UserEntity createdEntity = userRepository.save(entity);
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

    @Transactional
    public UserDto getSelfProfile() {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((JwtUserData)authentication).getLogin();

        return getByLogin(username);
    }

    public UserDto getByLogin(String login) {

        if (userRepository.findByLogin(login).isEmpty()) {
            throw new UserNotFoundException("User " + login + " was not found");
        }

        UserEntity entity = userRepository.findByLogin(login).get();

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

    public boolean checkByIdAndName(UserFriendDto dto) {

        return userRepository.getByUuidAndFullName(dto.getFriendId(), dto.getFriendName()).isPresent();
    }

    public boolean checkById(UserFriendDto dto) {

        return userRepository.getByUuid(dto.getFriendId()).isPresent();
    }

    public UserFriendDto getById(UserFriendDto dto) {

        UserEntity entity = userRepository.getByUuid(dto.getFriendId()).get();

        return new UserFriendDto(
                entity.getUuid(),
                entity.getFullName()
        );
    }

    @Transactional
    public List<UserDto> getFiltered(PaginationDto dto){

        Map<String, String> filters = dto.getFilters();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date;
        if (filters.get("birthDate") == null) {
            date = null;
        } else {
            try {
                date = formatter.parse(filters.get("birthDate"));
            } catch (ParseException e) {
                throw new DateParseException("Invalid date format");
            }
        }

        UserEntity example = UserEntity
                .builder()
                .login(filters.get("login"))
                .fullName(filters.get("fullName"))
                .birthDate(date)
                .phoneNumber(filters.get("phoneNumber"))
                .city(filters.get("city"))
                .build();

        Map<String, String> sortingMap = dto.getSorting();
        Sort sort = null;
        Sort fullSort = null;
        if (!sortingMap.isEmpty()) {
            for (String field : sortingMap.keySet()) {
                String order = sortingMap.get(field);
                sort = order.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(field).ascending()
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

        Page<UserEntity> entities = userRepository.findAll(Example.of(example),
                PageRequest.of(dto.getPageNo() - 1, dto.getPageSize(), sort));

        List<UserDto> dtos = new ArrayList<>();
        for(UserEntity entity : entities) {
            dtos.add(new UserDto(
                    entity.getLogin(),
                    entity.getEmail(),
                    entity.getFullName(),
                    entity.getBirthDate(),
                    entity.getPhoneNumber(),
                    entity.getCity(),
                    entity.getAvatar()
            ));
        }
        return dtos;
    }
}
