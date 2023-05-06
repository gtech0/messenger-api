package com.labs.java_lab1.notif.repository;

import com.labs.java_lab1.common.dto.NotifTypeEnum;
import com.labs.java_lab1.notif.entity.NotificationEntity;
import com.labs.java_lab1.notif.entity.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {
    Integer countByUserIdAndStatusAndType(String userId, StatusEnum status, NotifTypeEnum type);
    Optional<NotificationEntity> getByUuidAndUserId(String id, String userId);
}
