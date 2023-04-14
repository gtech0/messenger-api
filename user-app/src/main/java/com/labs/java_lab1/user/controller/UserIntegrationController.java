package com.labs.java_lab1.user.controller;

import com.labs.java_lab1.user.dto.UserFriendDto;
import com.labs.java_lab1.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/integration/users")
@RequiredArgsConstructor
public class UserIntegrationController {

    private final UserService userService;

    @PostMapping("/checkidname")
    public boolean userExistsByIdName(@RequestBody UserFriendDto dto) {
        return userService.checkByIdAndName(dto);
    }

    @PostMapping("/checkid")
    public boolean userExistsById(@RequestBody UserFriendDto dto) {
        return userService.checkById(dto);
    }

    @PostMapping("/sync")
    public UserFriendDto userGetById(@RequestBody UserFriendDto dto) {
        return userService.getById(dto);
    }

}