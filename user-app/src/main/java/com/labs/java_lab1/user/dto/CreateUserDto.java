package com.labs.java_lab1.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Data
@AllArgsConstructor(staticName = "of")
public class CreateUserDto {

    @NotBlank(message = "Login is required")
    private String login;

    @Email(message = "Email is incorrect", regexp = "[A-Za-z0-9]+@[A-Za-z0-9]+\\.[A-Za-z]+")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Name is required")
    private String fullName;

    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;

    private String phoneNumber;

    private String city;

    private String avatar;

}
