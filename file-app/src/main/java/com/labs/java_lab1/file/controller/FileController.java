package com.labs.java_lab1.file.controller;

import com.labs.java_lab1.common.dto.FileIdNameSizeDto;
import com.labs.java_lab1.file.storage.FileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @SneakyThrows
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<FileIdNameSizeDto> upload(@RequestParam("files") List<MultipartFile> file) {
        return fileService.upload(file);
    }

    @SneakyThrows
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping(value = "/download/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> download(@PathVariable("id") String id) {
        var content = fileService.download(id);
        return ResponseEntity.ok()
                .body(content);
    }
}
