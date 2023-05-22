package com.labs.java_lab1.friends.service;

import com.labs.java_lab1.common.security.JwtUserData;
import com.labs.java_lab1.friends.dto.*;
import com.labs.java_lab1.friends.entity.FriendsEntity;
import com.labs.java_lab1.friends.repository.FriendsRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class FriendsServiceTest {

    @InjectMocks
    private FriendsService friendsService;

    @Mock
    private FriendsRepository friendsRepository;

    @Test
    public void getFriends() {
        UUID userId = UUID.fromString("540af0db-4862-40f1-8934-385f4da10ae1");

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = mock(Authentication.class);
        JwtUserData jwtUserData = new JwtUserData(userId,"gqwrhrehr", "Иванов Иван Иваныч");
        when(authentication.getPrincipal()).thenReturn(jwtUserData);
        context.setAuthentication(authentication);

        List<GetFriendsDto> getFriendsDtoList = List.of(getFriendsDto());
        Pageable pageable = PageRequest.of(0, 1);
        when(friendsRepository.findAllByUserIdAndDeleteDateAndFriendNameContaining(
                eq(userId.toString()),
                eq(null),
                anyString(),
                eq(pageable)
        ))
                .thenReturn(List.of(friendsEntity()));

        PaginationDto paginationDto = new PaginationDto(
                1,
                1,
                ""
        );

        List<GetFriendsDto> getFriendsDto = friendsService.getFriends(paginationDto);
        assert Objects.equals(getFriendsDto, getFriendsDtoList);
    }

    @Test
    public void getFriend() {
        UUID userId = UUID.fromString("540af0db-4862-40f1-8934-385f4da10ae1");
        String friendId = "640af0db-4862-40f1-8934-385f4da10ae1";

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = mock(Authentication.class);
        JwtUserData jwtUserData = new JwtUserData(userId,"gqwrhrehr", "Иванов Иван Иваныч");
        when(authentication.getPrincipal()).thenReturn(jwtUserData);
        context.setAuthentication(authentication);

        when(friendsRepository.getByUserIdAndFriendId(userId.toString(), friendId))
                .thenReturn(Optional.of(friendsEntity()));

        FriendDto friendDto = friendsService.getFriend(friendId);
        FriendDto friendDtoTest = friendDto();
        assert Objects.equals(friendDto, friendDtoTest);
    }

    @Test
    public void friendExists() {
        UUID userId = UUID.fromString("540af0db-4862-40f1-8934-385f4da10ae1");
        String friendId = "640af0db-4862-40f1-8934-385f4da10ae1";

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = mock(Authentication.class);
        JwtUserData jwtUserData = new JwtUserData(userId,"gqwrhrehr", "Иванов Иван Иваныч");
        when(authentication.getPrincipal()).thenReturn(jwtUserData);
        context.setAuthentication(authentication);

        when(friendsRepository.getByUserIdAndFriendId(userId.toString(), friendId))
                .thenReturn(Optional.of(friendsEntity()));

        assert friendsService.friendExists(friendId);
    }

    @Test
    public void searchFriends() {
        UUID userId = UUID.fromString("540af0db-4862-40f1-8934-385f4da10ae1");

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = mock(Authentication.class);
        JwtUserData jwtUserData = new JwtUserData(userId,"gqwrhrehr", "Иванов Иван Иваныч");
        when(authentication.getPrincipal()).thenReturn(jwtUserData);
        context.setAuthentication(authentication);

        Pageable pageable = PageRequest.of(0, 1);

        when(friendsRepository
                .findAllQuery(
                        null,
                        userId.toString(),
                        null,
                        null,
                        pageable
                )
        )
                .thenReturn(List.of(friendsEntity()));

        List<GetFriendsDto> getFriendsDtoListTest = List.of(getFriendsDto());
        SearchDto searchDto = new SearchDto(
                1,
                1,
                new HashMap<>()
        );
        List<GetFriendsDto> getFriendsDtoList = friendsService.searchFriends(searchDto);
        assert Objects.equals(getFriendsDtoList, getFriendsDtoListTest);
    }

    @Test
    public void checkFriends() {
        UUID userId = UUID.fromString("540af0db-4862-40f1-8934-385f4da10ae1");
        String friendId = "640af0db-4862-40f1-8934-385f4da10ae1";
        when(friendsRepository.getByUserIdAndFriendId(userId.toString(), friendId))
                .thenReturn(Optional.of(friendsEntity()));

        ChatFriendDto dto = new ChatFriendDto(
                userId.toString(),
                friendId
        );
        assert friendsService.checkById(dto);
    }

    @SneakyThrows
    private FriendsEntity friendsEntity() {
        return new FriendsEntity(
                "662e8bcb-8e86-45ed-8a29-fcd0ff34a810",
                new Date(123),
                null,
                "540af0db-4862-40f1-8934-385f4da10ae1",
                "640af0db-4862-40f1-8934-385f4da10ae1",
                "grgreh"
        );
    }

    @SneakyThrows
    private GetFriendsDto getFriendsDto() {
        return new GetFriendsDto(
                new Date(123),
                null,
                "640af0db-4862-40f1-8934-385f4da10ae1",
                "grgreh"
        );
    }

    @SneakyThrows
    private FriendDto friendDto() {
        return new FriendDto(
                new Date(123),
                null,
                "540af0db-4862-40f1-8934-385f4da10ae1",
                "640af0db-4862-40f1-8934-385f4da10ae1",
                "grgreh"
        );
    }
}
