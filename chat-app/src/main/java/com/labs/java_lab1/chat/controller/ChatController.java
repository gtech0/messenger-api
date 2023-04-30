package com.labs.java_lab1.chat.controller;

import com.labs.java_lab1.chat.dto.SendFriendMessageDto;
import com.labs.java_lab1.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService service;

    @PostMapping("/send/friend")
    public ResponseEntity<SendFriendMessageDto> sendFriendMessage(@Valid @RequestBody SendFriendMessageDto dto) {
        return service.sendFriendMessage(dto);
    }

}
