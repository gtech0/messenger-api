package com.labs.java_lab1.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class ChatInfoDto {

    private String name;

    private String avatar;

    private String admin;

    private Date creationDate;

}
