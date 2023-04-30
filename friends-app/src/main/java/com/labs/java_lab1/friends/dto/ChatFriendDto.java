package com.labs.java_lab1.friends.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatFriendDto {

    private String userId;

    private String friendId;

}
