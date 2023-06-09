package com.labs.java_lab1.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String login;

    private String email;

    private String fullName;

    private Date birthDate;

    private String phoneNumber;

    private String city;

    private String avatar;

}
