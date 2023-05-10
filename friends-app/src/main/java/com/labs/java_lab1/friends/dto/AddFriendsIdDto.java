package com.labs.java_lab1.friends.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddFriendsIdDto {

    @NotBlank(message = "Friend id is required")
    private String friendId;

}
