package com.labs.java_lab1.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "chat_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatUserEntity {

    @Id
    @Column(name = "id")
    private String uuid;

    @Column(name = "chat_id")
    private String chatId;

    @Column(name = "user_id")
    private String userId;
}
