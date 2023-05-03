package com.labs.java_lab1.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatListPaginationDto {

    private Integer pageNo;

    private Integer pageSize;

    private String name;

}
