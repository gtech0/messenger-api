package com.labs.java_lab1.user.service;

import com.labs.java_lab1.user.dto.CreateUpdateUserDto;
import com.labs.java_lab1.user.dto.PaginationDto;
import com.labs.java_lab1.user.dto.UserDto;
import com.labs.java_lab1.user.entity.UserEntity;
import com.labs.java_lab1.user.exception.UniqueConstraintViolationException;
import com.labs.java_lab1.user.exception.UserNotFoundException;
import com.labs.java_lab1.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDto save(CreateUpdateUserDto dto) {

        if (userRepository.existsByLogin(dto.getLogin())) {
            throw new UniqueConstraintViolationException("User " + dto.getLogin() + " already exists");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UniqueConstraintViolationException("Email " + dto.getEmail() + " is already used");
        }

        UserEntity entity = new UserEntity(
                UUID.randomUUID().toString(),
                dto.getLogin(),
                dto.getEmail(),
                dto.getPassword(),
                dto.getFullName(),
                dto.getBirthDate(),
                dto.getPhoneNumber(),
                dto.getCity(),
                dto.getAvatar(),
                new Date()
        );

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
    public UserDto update(CreateUpdateUserDto dto, String login) {

        if (!userRepository.existsByLogin(login)) {
            throw new UserNotFoundException("User " + login + " was not found");
        }

        UserEntity entity = userRepository.findByLogin(login);
        entity.setEmail(dto.getEmail());
        entity.setPassword(dto.getPassword());
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
    public UserDto getByLogin(String login) {

        if (!userRepository.existsByLogin(login)) {
            throw new UserNotFoundException("User " + login + " was not found");
        }

        UserEntity entity = userRepository.findByLogin(login);

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

    @Transactional
    public boolean existsByLogin(String login) {
        return userRepository.existsByLogin(login);
    }

    @Transactional
    public List<UserDto> getFiltered(PaginationDto dto) throws ParseException {

        Map<String, String> filters = dto.getFilters();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date;
        if (filters.get("birthDate") == null) {
            date = null;
        } else {
            date = formatter.parse(filters.get("birthDate"));
        }

        UserEntity example = UserEntity
                .builder()
                .fullName(filters.get("fullName"))
                .birthDate(date)
                .phoneNumber(filters.get("phoneNumber"))
                .city(filters.get("city"))
                .build();

        Page<UserEntity> entities = userRepository.findAll(Example.of(example), PageRequest.of(dto.getPageNo() - 1, dto.getPageSize()));

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
