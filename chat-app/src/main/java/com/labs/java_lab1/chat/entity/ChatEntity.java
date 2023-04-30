package com.labs.java_lab1.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "chat",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "friend_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatEntity {

    @Id
    @Column(name = "id")
    private String uuid;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "friend_id")
    private String friendId;

    @Enumerated(EnumType.STRING)
    @Column
    private ChatTypeEnum type;

    @Column
    private String name;

    @Column(name = "admin_id")
    private String adminId;

    @Temporal(TemporalType.DATE)
    @Column(name = "creation_date")
    private Date creationDate;

    @Column
    private String avatar;
}
