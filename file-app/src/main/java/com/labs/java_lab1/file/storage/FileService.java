package com.labs.java_lab1.file.storage;

import com.labs.java_lab1.common.dto.FileDataDto;
import com.labs.java_lab1.common.dto.FileIdNameDto;

public interface FileService {

    FileIdNameDto upload(FileDataDto content);

    byte[] download(String id);

}
