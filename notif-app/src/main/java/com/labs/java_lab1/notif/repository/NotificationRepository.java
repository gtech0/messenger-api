package com.labs.java_lab1.notif.repository;

import com.labs.java_lab1.common.dto.NotifTypeEnum;
import com.labs.java_lab1.notif.dto.NotifListDto;
import com.labs.java_lab1.notif.entity.NotificationEntity;
import com.labs.java_lab1.notif.entity.StatusEnum;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {
    Integer countByUserIdAndStatusAndType(String userId, StatusEnum status, NotifTypeEnum type);
    Optional<NotificationEntity> getByUuidAndUserId(String id, String userId);
    @Query(value =
            "SELECT DISTINCT new com.labs.java_lab1.notif.dto.NotifListDto(" +
                    "notif.uuid, notif.type, notif.text, notif.status, notif.receivedDate) " +
                    "FROM NotificationEntity notif " +
                    "WHERE notif.userId = :userId " +
                    "AND (cast(:startDate as timestamp) IS NULL AND cast(:endDate as timestamp) IS NULL " +
                    "OR notif.receivedDate >= :startDate AND cast(:endDate as timestamp) IS NULL " +
                    "OR notif.receivedDate <= :endDate AND cast(:startDate as timestamp) IS NULL " +
                    "OR notif.receivedDate BETWEEN :startDate AND :endDate) " +
                    "AND (notif.text LIKE CONCAT('%', :text, '%') OR :text IS NULL)" +
                    "AND (notif.type IN :types) " +
                    "ORDER BY notif.receivedDate DESC")
    List<NotifListDto> getAllByFilters(String userId,
                                       Date startDate,
                                       Date endDate,
                                       String text,
                                       List<NotifTypeEnum> types,
                                       Pageable pageable);
}
