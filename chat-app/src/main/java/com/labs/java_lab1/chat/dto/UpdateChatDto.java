package com.labs.java_lab1.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UpdateChatDto {

    private String id;

    private String name;

    private String avatar;

    private List<String> users;

}
