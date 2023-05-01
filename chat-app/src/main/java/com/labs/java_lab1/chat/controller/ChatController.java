package com.labs.java_lab1.chat.controller;

import com.labs.java_lab1.chat.dto.*;
import com.labs.java_lab1.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService service;

    @PostMapping("/message/private")
    public ResponseEntity<SendMessageDto> sendFriendMessage(@Valid @RequestBody SendMessageDto dto) {
        return service.sendFriendMessage(dto);
    }

    @PostMapping("/message/public")
    public ResponseEntity<SendMessageDto> sendChatMessage(@Valid @RequestBody SendMessageDto dto) {
        return service.sendChatMessage(dto);
    }

    @PostMapping("/create")
    public ResponseEntity<CreateChatDto> createChat(@RequestBody CreateChatDto dto) {
        return service.createChat(dto);
    }

    @PutMapping("/update")
    public ResponseEntity<UpdateChatDto> updateChat(@RequestBody UpdateChatDto dto) {
        return service.updateChat(dto);
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<ChatInfoDto> chatInfo(@PathVariable String id) {
        return service.chatInfo(id);
    }

    @GetMapping("/messages/{id}")
    public ResponseEntity<List<MessageInfoDto>> getChatMessages(@PathVariable String id) {
        return service.viewChat(id);
    }

}
