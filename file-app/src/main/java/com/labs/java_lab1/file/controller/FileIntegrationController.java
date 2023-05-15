package com.labs.java_lab1.file.controller;

import com.labs.java_lab1.common.dto.FileDataDto;
import com.labs.java_lab1.common.dto.FileIdNameDto;
import com.labs.java_lab1.file.storage.FileService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/integration/files")
@RequiredArgsConstructor
public class FileIntegrationController {

    private final FileService fileService;

    @SneakyThrows
    @PostMapping("/upload")
    public FileIdNameDto upload(@RequestBody FileDataDto file) {
        return fileService.upload(file);
    }
}
