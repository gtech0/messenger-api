package com.labs.java_lab1.friends.dto;

import lombok.Data;

@Data
public class PagiantionDto {

    private final Integer pageNo;

    private final Integer pageSize;

    private final String friendName;

}
