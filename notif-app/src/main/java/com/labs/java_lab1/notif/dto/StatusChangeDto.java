package com.labs.java_lab1.notif.dto;

import com.labs.java_lab1.notif.entity.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusChangeDto {

    List<String> notifications;

    StatusEnum status;

}
