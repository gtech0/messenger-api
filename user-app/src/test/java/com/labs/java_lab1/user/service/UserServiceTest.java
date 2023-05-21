package com.labs.java_lab1.user.service;

import com.labs.java_lab1.common.dto.UserMessageInfoDto;
import com.labs.java_lab1.user.dto.UserFriendDto;
import com.labs.java_lab1.user.dto.UserFriendIdDto;
import com.labs.java_lab1.user.dto.UserIdDto;
import com.labs.java_lab1.user.entity.UserEntity;
import com.labs.java_lab1.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void checkByIdAndName() {
        String uuid = "2ffc01bd-e453-4795-a312-0c6ee8461e08";
        String name = "Иванов Иван Иваныч";
        when(userRepository.getByUuidAndFullName(eq(uuid), eq(name)))
                .thenReturn(Optional.of(userEntity()));

        assert userService.checkByIdAndName(new UserFriendDto(uuid, name));
    }

    @Test
    public void checkById() {
        String uuid = "2ffc01bd-e453-4795-a312-0c6ee8461e08";
        when(userRepository.getByUuid(eq(uuid)))
                .thenReturn(Optional.of(userEntity()));

        assert userService.checkById(new UserFriendIdDto(uuid));
    }

    @Test
    public void getFriendById() {
        String uuid = "2ffc01bd-e453-4795-a312-0c6ee8461e08";
        when(userRepository.getByUuid(eq(uuid)))
                .thenReturn(Optional.of(userEntity()));

        assert Objects.equals(userService.getFriendById(new UserFriendIdDto(uuid)).getFriendId(), uuid);
    }

    @Test
    public void getMessageInfoById() {
        String uuid = "2ffc01bd-e453-4795-a312-0c6ee8461e08";
        String name = "Иванов Иван Иваныч";
        String avatar = "08f1baa5-7710-4c4f-babb-351bb2519d02";
        when(userRepository.getByUuid(eq(uuid)))
                .thenReturn(Optional.of(userEntity()));

        UserMessageInfoDto user = userService.getMessageInfoById(new UserIdDto(uuid));

        assert Objects.equals(user.getFullName(), name) &&
                Objects.equals(user.getAvatar(), avatar);
    }

    @SneakyThrows
    private UserEntity userEntity() {
        return new UserEntity(
                "2ffc01bd-e453-4795-a312-0c6ee8461e08",
                "gqwrhrehr",
                "ivan@tsu.ru",
                "1",
                "Иванов Иван Иваныч",
                new Date(1684236437),
                "592386923",
                "city",
                "08f1baa5-7710-4c4f-babb-351bb2519d02",
                new Date()
        );
    }
}
