package com.labs.java_lab1.friends.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SearchDto {

    private final Integer pageNo;

    private final Integer pageSize;

    private final Map<String, String> filters;

}
