package com.labs.java_lab1.chat.dto;

import com.labs.java_lab1.common.dto.FileIdNameSizeDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageInfoDto {

    private String id;

    private Date sentDate;

    private String text;

    private String fullName;

    private String avatar;

    private List<FileIdNameSizeDto> attachments;

}
