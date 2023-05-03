package com.labs.java_lab1.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "chat_user",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_id", "user_id"}))
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
