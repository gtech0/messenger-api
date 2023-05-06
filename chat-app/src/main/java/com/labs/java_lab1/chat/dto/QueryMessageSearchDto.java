package com.labs.java_lab1.chat.dto;

import com.labs.java_lab1.chat.entity.ChatTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryMessageSearchDto {

    private String id;

    private String name;

    private String message;

    private Date sentDate;

    private ChatTypeEnum type;

    private String userId;

    private String fullName;

    private String messageId;

}
