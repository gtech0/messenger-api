package com.labs.java_lab1.friends.controller;

import com.labs.java_lab1.friends.dto.ChatFriendDto;
import com.labs.java_lab1.friends.service.BlacklistService;
import com.labs.java_lab1.friends.service.FriendsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/integration/friends")
@RequiredArgsConstructor
public class FriendsIntegrationController {

    private final FriendsService friendsService;
    private final BlacklistService blacklistService;

    @PostMapping("/check-if-friend")
    public boolean checkIfFriend(@RequestBody ChatFriendDto dto) {
        return friendsService.checkById(dto);
    }

    @PostMapping("/check-if-blacklisted")
    public boolean checkBlacklist(@RequestBody ChatFriendDto dto) {
        return blacklistService.checkById(dto);
    }

}
