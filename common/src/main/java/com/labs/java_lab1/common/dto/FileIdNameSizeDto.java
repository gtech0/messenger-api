package com.labs.java_lab1.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileIdNameSizeDto {

    private String id;

    private String fileName;

    private long size;

}
