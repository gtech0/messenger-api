package com.labs.java_lab1.friends.controller;

import com.labs.java_lab1.friends.dto.AddFriendsDto;
import com.labs.java_lab1.friends.service.FriendsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendsController {

    private final FriendsService friendsService;

    @PostMapping
    public AddFriendsDto addFriend(@Valid @RequestBody AddFriendsDto dto) {
        return friendsService.save(dto);
    }

}
