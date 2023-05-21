package com.labs.java_lab1.chat.controller;

import com.labs.java_lab1.chat.dto.*;
import com.labs.java_lab1.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService service;

    @PostMapping("/message/private")
    public ResponseEntity<SendChatMessageDto> sendFriendMessage(@Valid @RequestPart("dto") SendChatMessageDto dto,
                                                                @RequestPart("files") List<MultipartFile> files) throws IOException {
        return service.sendFriendMessage(dto, files);
    }

    @PostMapping("/message/public")
    public ResponseEntity<SendChatMessageDto> sendChatMessage(@Valid @RequestPart("dto") SendChatMessageDto dto,
                                                              @RequestPart("files") List<MultipartFile> files) throws IOException {
        return service.sendChatMessage(dto, files);
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

    @PostMapping("/list")
    public ResponseEntity<List<ChatListDto>> getChatList(@RequestBody ChatListPaginationDto dto) {
        return service.viewChatList(dto);
    }

    @PostMapping("/messages/search")
    public ResponseEntity<List<MessageSearchDto>> messageSearch(@RequestBody MessageSearchBodyDto dto) {
        return service.messageSearch(dto);
    }

}
