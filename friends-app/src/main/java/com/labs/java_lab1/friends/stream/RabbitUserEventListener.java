package com.labs.java_lab1.friends.stream;

import com.labs.java_lab1.common.dto.UserSyncDto;
import com.labs.java_lab1.friends.entity.BlacklistEntity;
import com.labs.java_lab1.friends.entity.FriendsEntity;
import com.labs.java_lab1.friends.repository.BlacklistRepository;
import com.labs.java_lab1.friends.repository.FriendsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitUserEventListener {

    private final FriendsRepository friendsRepository;
    private final BlacklistRepository blacklistRepository;

    @Bean
    public Consumer<UserSyncDto> userModifiedEvent() {
        return message -> {
            List<FriendsEntity> friendList = friendsRepository.getAllByFriendId(message.getUserId());
            for (FriendsEntity friend : friendList) {
                friend.setFriendName(message.getFullName());
                friendsRepository.save(friend);
            }

            List<BlacklistEntity> blackList = blacklistRepository.getAllByFriendId(message.getUserId());
            for (BlacklistEntity blacklisted : blackList) {
                blacklisted.setFriendName(message.getFullName());
                blacklistRepository.save(blacklisted);
            }
        };
    }
}
