package com.labs.java_lab1.user.controller;


import com.labs.java_lab1.user.dto.CreateUpdateUserDto;
import com.labs.java_lab1.user.dto.PaginationDto;
import com.labs.java_lab1.user.dto.UserDto;
import com.labs.java_lab1.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    public final UserService userService;

    @PostMapping
    public UserDto create(@Valid @RequestBody CreateUpdateUserDto dto) {
        return userService.save(dto);
    }

    @PutMapping("/{login}")
    public UserDto update(@RequestBody CreateUpdateUserDto dto, @PathVariable String login) {
        return userService.update(dto, login);
    }

    @GetMapping("/{login}")
    public UserDto getByLogin(@PathVariable String login) {
        return userService.getByLogin(login);
    }

    @PostMapping("/list")
    public List<UserDto> getUsers(@RequestBody PaginationDto dto) throws ParseException {
        return userService.getFiltered(dto);
    }
}
