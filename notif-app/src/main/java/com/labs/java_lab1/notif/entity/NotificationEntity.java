package com.labs.java_lab1.notif.entity;

import com.labs.java_lab1.common.dto.NotifTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {

    @Id
    @Column(name = "id")
    private String uuid;

    @Column
    private NotifTypeEnum type;

    @Column
    private String text;

    @Column(name = "user_id")
    private String userId;

    @Column
    private StatusEnum status;

    @Column(name = "received_date")
    private Date receivedDate;

}
