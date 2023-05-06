package com.labs.java_lab1.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifDto {

    private String userId;

    private NotifTypeEnum type;

    private String notifMessage;

}
