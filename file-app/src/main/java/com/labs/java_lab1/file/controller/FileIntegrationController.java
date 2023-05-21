package com.labs.java_lab1.file.controller;

import com.labs.java_lab1.common.dto.FileDataDto;
import com.labs.java_lab1.common.dto.FileIdNameSizeDto;
import com.labs.java_lab1.file.storage.FileService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/integration/files")
@RequiredArgsConstructor
public class FileIntegrationController {

    private final FileService fileService;

    @SneakyThrows
    @PostMapping("/upload")
    public FileIdNameSizeDto upload(@RequestBody FileDataDto file) {
        return fileService.upload(file);
    }

    @SneakyThrows
    @GetMapping(value = "/download/{id}")
    public ResponseEntity<Boolean> download(@PathVariable String id) {
        return ResponseEntity.ok(fileService.checkFile(id));
    }
}
