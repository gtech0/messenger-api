package com.labs.java_lab1.friends.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddFriendsDto {

    @NotBlank(message = "Friend id is required")
    private String friendId;

    @NotBlank(message = "Name is required")
    private String friendName;
}
