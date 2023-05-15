package com.labs.java_lab1.file.controller;

import com.labs.java_lab1.common.dto.FileDataDto;
import com.labs.java_lab1.common.dto.FileIdNameDto;
import com.labs.java_lab1.file.storage.FileService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @SneakyThrows
    @PostMapping("/upload")
    public FileIdNameDto upload(@RequestBody FileDataDto file) {
        return fileService.upload(file);
    }

    @SneakyThrows
    @GetMapping(value = "/download/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> download(@PathVariable("id") String id) {
        var content = fileService.download(id);
        return ResponseEntity.ok()
                .body(content);
    }
}
