package com.labs.java_lab1.notif.dto;

import com.labs.java_lab1.common.dto.NotifTypeEnum;
import com.labs.java_lab1.notif.entity.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifListDto {

    private String id;

    private NotifTypeEnum type;

    private String text;

    private StatusEnum status;

    private Date receivedDate;

}
