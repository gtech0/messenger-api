package com.labs.java_lab1.friends.controller;

import com.labs.java_lab1.friends.dto.*;
import com.labs.java_lab1.friends.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/friends/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    @PostMapping("/list")
    public List<GetFriendsDto> getFriends(@RequestBody PagiantionDto dto) {
        return blacklistService.getBlacklist(dto);
    }

    @PostMapping("/add")
    public AddFriendsDto addFriend(@Valid @RequestBody AddFriendsIdDto dto) {
        return blacklistService.save(dto);
    }

    @GetMapping("/{id}")
    public FriendDto getFriend(@PathVariable String id) {
        return blacklistService.getPerson(id);
    }

    @PatchMapping("/sync/{id}")
    public AddFriendsDto syncFriend(@PathVariable String id) {
        return blacklistService.syncPerson(id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DeleteFriendDto> deleteFriend(@PathVariable String id) {
        return blacklistService.deletePerson(id);
    }

    @PostMapping("/search")
    public List<GetFriendsDto> searchFriends(@RequestBody SearchDto dto) {
        return blacklistService.searchBlacklist(dto);
    }

    @GetMapping("/check/{id}")
    public boolean checkBlacklist(@PathVariable String id) {
        return blacklistService.personExists(id);
    }
}
