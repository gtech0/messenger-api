package com.labs.java_lab1.user.service;

import com.labs.java_lab1.user.dto.*;
import com.labs.java_lab1.user.entity.Role;
import com.labs.java_lab1.user.entity.UserEntity;
import com.labs.java_lab1.user.exception.DateParseException;
import com.labs.java_lab1.user.exception.UniqueConstraintViolationException;
import com.labs.java_lab1.user.exception.UserNotFoundException;
import com.labs.java_lab1.user.repository.UserRepository;
import com.labs.java_lab1.user.response.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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
                new Date(),
                Role.USER
        );

        UserEntity createdEntity = userRepository.save(entity);
        String token = jwtService.generateToken(createdEntity);
        return AuthenticationResponse
                .builder()
                .token(token)
                .build();
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthDto dto) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getLogin(),
                        dto.getPassword()
                )
        );

        var entity = userRepository.findByLogin(dto.getLogin())
                .orElseThrow();
        String token = jwtService.generateToken(entity);
        return AuthenticationResponse
                .builder()
                .token(token)
                .build();
    }

    @Transactional
    public UserDto update(UpdateUserDto dto) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();

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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return getByLogin(authentication.getName());
    }

    @Transactional
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
