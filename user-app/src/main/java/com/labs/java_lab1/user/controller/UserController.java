package com.labs.java_lab1.user.controller;


import com.labs.java_lab1.user.dto.*;
import com.labs.java_lab1.common.response.AuthenticationResponse;
import com.labs.java_lab1.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    public final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> create(@Valid @RequestBody CreateUserDto dto) {
        return ResponseEntity.ok(userService.save(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthDto dto) {
        return ResponseEntity.ok(userService.authenticate(dto));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> update(@RequestBody UpdateUserDto dto) {
        return ResponseEntity.ok(userService.update(dto));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getSelfProfile() {
        return ResponseEntity.ok(userService.getSelfProfile());
    }

    @GetMapping("/list/{login}")
    public ResponseEntity<UserDto> getByLogin(@PathVariable String login) {
        return ResponseEntity.ok(userService.getByLogin(login));
    }

    @PostMapping("/list")
    public ResponseEntity<List<UserDto>> getUsers(@RequestBody PaginationDto dto){
        return ResponseEntity.ok(userService.getFiltered(dto));
    }

}
