package com.labs.java_lab1.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateChatDto {

    private String name;

    private String avatar;

    List<String> users;

}
