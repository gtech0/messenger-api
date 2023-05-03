package com.labs.java_lab1.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageSearchDto {

    private String id;

    private String name;

    private String message;

    private Date sentDate;

    private String attachmentName;

}
