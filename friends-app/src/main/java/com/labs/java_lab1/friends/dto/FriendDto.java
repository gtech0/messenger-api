package com.labs.java_lab1.friends.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendDto {

    private Date addDate;

    private Date deleteDate;

    private String userId;

    private String friendId;

    private String friendName;

}
