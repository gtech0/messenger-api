package com.labs.java_lab1.friends.controller;

import com.labs.java_lab1.friends.dto.AddFriendsDto;
import com.labs.java_lab1.friends.dto.DeleteFriendDto;
import com.labs.java_lab1.friends.dto.FriendDto;
import com.labs.java_lab1.friends.service.FriendsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendsController {

    private final FriendsService friendsService;

    @PostMapping("/add")
    public AddFriendsDto addFriend(@Valid @RequestBody AddFriendsDto dto) {
        return friendsService.save(dto);
    }

    @GetMapping("/{id}")
    public FriendDto getFriend(@PathVariable String id) {
        return friendsService.getFriend(id);
    }

    @PatchMapping("/sync/{id}")
    public AddFriendsDto syncFriend(@PathVariable String id) {
        return friendsService.syncFriend(id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DeleteFriendDto> deleteFriend(@PathVariable String id) {
        return friendsService.deleteFriend(id);
    }

}
