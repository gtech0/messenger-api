package com.labs.java_lab1.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendFriendMessageDto {

    private String friendId;

    @Size(min = 1, max = 500)
    private String text;

    private List<String> attachments;

}
