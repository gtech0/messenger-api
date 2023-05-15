package com.labs.java_lab1.notif.controller;

import com.labs.java_lab1.notif.dto.NotifListDto;
import com.labs.java_lab1.notif.dto.NotifListPaginationDto;
import com.labs.java_lab1.notif.dto.StatusChangeDto;
import com.labs.java_lab1.notif.dto.UnreadDto;
import com.labs.java_lab1.notif.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/search")
    public List<NotifListDto> notifList(@RequestBody NotifListPaginationDto dto) {
        return service.notifList(dto);
    }
}
