package com.labs.java_lab1.friends.service;

import com.labs.java_lab1.common.exception.UniqueConstraintViolationException;
import com.labs.java_lab1.common.security.JwtUserData;
import com.labs.java_lab1.friends.dto.AddFriendsDto;
import com.labs.java_lab1.friends.entity.FriendsEntity;
import com.labs.java_lab1.friends.repository.FriendsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendsService {

    private final FriendsRepository friendsRepository;

    public AddFriendsDto save(AddFriendsDto dto) {

        Object authentication = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JwtUserData data = (JwtUserData)authentication;

        if (Objects.equals(dto.getFriendId(), data.getId().toString())) {
            throw new UniqueConstraintViolationException("You can't add yourself");
        }

        FriendsEntity entity = new FriendsEntity(
                UUID.randomUUID().toString(),
                new Date(),
                null,
                data.getId().toString(),
                dto.getFriendId(),
                dto.getFriendName()
        );

        FriendsEntity createdEntity = friendsRepository.save(entity);
        return new AddFriendsDto(
                createdEntity.getFriendId(),
                createdEntity.getFriendName()
        );
    }
}
