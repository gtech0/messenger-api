package com.labs.java_lab1.notif.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifListPaginationDto {

    private Integer pageNo;

    private Integer pageSize;

    private String startDate;

    private String endDate;

    private String text;

    private List<String> types;

}
