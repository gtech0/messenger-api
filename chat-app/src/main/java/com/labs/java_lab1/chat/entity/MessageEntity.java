package com.labs.java_lab1.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "message")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {

    @Id
    @Column(name = "id")
    private String uuid;

    @Column(name = "chat_id")
    private String chatId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "full_name")
    private String fullName;

    @Column
    private String avatar;

    @Temporal(TemporalType.DATE)
    @Column(name = "sent_date")
    private Date sentDate;

    @Column
    private String message;

    @OneToMany(mappedBy = "message", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<AttachmentEntity> attachments;
}
