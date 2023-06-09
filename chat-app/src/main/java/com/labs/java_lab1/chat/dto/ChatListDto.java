package com.labs.java_lab1.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatListDto {

    private String id;

    private String name;

    private String message;

    private boolean attachmentsPresent;

    private Date sentDate;

    private String userId;

}
