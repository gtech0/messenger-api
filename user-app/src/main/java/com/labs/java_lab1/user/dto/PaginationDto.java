package com.labs.java_lab1.user.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class PaginationDto {

    private final Integer pageNo;

    private final Integer pageSize;

    private final Map<String, String> filters;

    private final Map<String, String> sorting;
}
