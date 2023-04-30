package com.labs.java_lab1.chat.repository;

import com.labs.java_lab1.chat.entity.AttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<AttachmentEntity, String> {
    Optional<AttachmentEntity> getByUuid(String id);
}
