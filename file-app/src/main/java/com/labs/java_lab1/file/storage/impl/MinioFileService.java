package com.labs.java_lab1.file.storage.impl;

import com.labs.java_lab1.common.dto.FileDataDto;
import com.labs.java_lab1.common.dto.FileIdNameSizeDto;
import com.labs.java_lab1.file.entity.FileEntity;
import com.labs.java_lab1.file.repository.FileRepository;
import com.labs.java_lab1.file.storage.FileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class MinioFileService implements FileService {

    private final FileRepository fileRepository;
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @PostConstruct
    public void init() {
        log.info("Minio configs: {}", minioConfig);
    }

    @Override
    public List<FileIdNameSizeDto> upload(List<MultipartFile> contents) {
        List<FileIdNameSizeDto> filedataList = new ArrayList<>();
        for (MultipartFile content : contents) {
            try {
                var id = UUID.randomUUID().toString();
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(minioConfig.getBucket())
                        .object(id)
                        .stream(new ByteArrayInputStream(content.getBytes()), content.getSize(), -1)
                        .build());

                fileRepository.save(new FileEntity(
                        id,
                        content.getOriginalFilename(),
                        content.getSize()
                ));

                filedataList.add(new FileIdNameSizeDto(
                        id,
                        content.getOriginalFilename(),
                        content.getSize()
                ));
            } catch (Exception e) {
                throw new RuntimeException("Upload error", e);
            }
        }
        return filedataList;
    }

    @Override
    public FileIdNameSizeDto upload(FileDataDto content) {
        try {
            var id = UUID.randomUUID().toString();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucket())
                    .object(id)
                    .stream(new ByteArrayInputStream(content.getBytes()), content.getBytes().length, -1)
                    .build());

            fileRepository.save(new FileEntity(
                    id,
                    content.getFileName(),
                    content.getBytes().length
            ));

            return new FileIdNameSizeDto(
                    id,
                    content.getFileName(),
                    content.getBytes().length
            );
        } catch (Exception e) {
            throw new RuntimeException("Upload error", e);
        }
    }

    @Override
    public byte[] download(String id) {
        var args = GetObjectArgs.builder()
                .bucket(minioConfig.getBucket())
                .object(id)
                .build();
        try (var in = minioClient.getObject(args)) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Download file error id=" + id, e);
        }
    }

    @Override
    public boolean checkFile(String id) {
        var args = GetObjectArgs.builder()
                .bucket(minioConfig.getBucket())
                .object(id)
                .build();
        try (var in = minioClient.getObject(args)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
