package com.labs.java_lab1.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSyncDto {

    private String userId;

    private String fullName;

}
