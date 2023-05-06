package com.labs.java_lab1.notif.controller;

import com.labs.java_lab1.notif.dto.StatusChangeDto;
import com.labs.java_lab1.notif.dto.UnreadDto;
import com.labs.java_lab1.notif.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping("/unread")
    public UnreadDto countUnreadMessages() {
        return service.countUnreadMessages();
    }

    @PostMapping("/change")
    public UnreadDto changeNotifStatus(@RequestBody StatusChangeDto dto) {
        return service.changeNotifStatus(dto);
    }

}
