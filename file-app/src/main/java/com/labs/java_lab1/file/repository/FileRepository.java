package com.labs.java_lab1.file.repository;

import com.labs.java_lab1.file.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, String> {
    Optional<FileEntity> findAllByUuid(String id);
}
