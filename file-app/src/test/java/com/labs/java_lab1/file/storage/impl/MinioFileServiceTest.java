package com.labs.java_lab1.file.storage.impl;

import com.labs.java_lab1.common.dto.FileIdNameSizeDto;
import com.labs.java_lab1.file.entity.FileEntity;
import com.labs.java_lab1.file.repository.FileRepository;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class MinioFileServiceTest {

    @InjectMocks
    private MinioFileService minioFileService;

    @Mock
    private MinioClient minioClient;

    @Mock
    private FileRepository repository;

    @Mock
    private MinioConfig minioConfig;

    @Test
    public void upload() throws Exception {
        List<MultipartFile> mockMultipartFileList = List.of(new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                "text".getBytes()
        ));

        when(minioConfig.getBucket()).thenReturn("bucket");

        List<FileIdNameSizeDto> fileIdNameSizeDtoList = minioFileService.upload(mockMultipartFileList);
        assert !fileIdNameSizeDtoList.isEmpty();
        verify(minioClient, times(1)).putObject(any());
        verify(repository, times(1)).save(any(FileEntity.class));
    }

    @Test
    public void download() throws Exception {
        byte[] content = "text".getBytes();
        String bucket = "bucket";
        String fileId = UUID.randomUUID().toString();

        FileEntity file = new FileEntity(
                fileId,
                "file",
                4
        );

        when(minioConfig.getBucket()).thenReturn(bucket);

        GetObjectResponse response = mock(GetObjectResponse.class);
        when(response.readAllBytes()).thenReturn(content);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(response);

        byte[] download = minioFileService.download(fileId);

        assertNotNull(download);
        assertArrayEquals(content, download);
    }
}
