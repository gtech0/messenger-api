package com.labs.java_lab1.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class MessageInfoDto {

    private String id;

    private Date sentDate;

    private String text;

    private String fullName;

    private String avatar;

}
