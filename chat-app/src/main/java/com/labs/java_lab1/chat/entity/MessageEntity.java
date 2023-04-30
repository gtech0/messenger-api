package com.labs.java_lab1.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

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

    @Column(name = "sent_date")
    private Date sentDate;

    @Column
    private String message;
}
