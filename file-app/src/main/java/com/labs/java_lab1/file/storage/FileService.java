package com.labs.java_lab1.file.storage;

import com.labs.java_lab1.common.dto.FileDataDto;
import com.labs.java_lab1.common.dto.FileIdNameSizeDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {

    List<FileIdNameSizeDto> upload(List<MultipartFile> content);

    FileIdNameSizeDto upload(FileDataDto content);

    byte[] download(String id);

    boolean checkFile(String id);

}
