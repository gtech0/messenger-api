package com.labs.java_lab1.user.dto;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class PaginationDto {

    private Integer pageNo;

    private Integer pageSize;

    private Map<String, String> filters;

    private Map<String, String> sorting;
}
