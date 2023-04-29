package com.labs.java_lab1.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "attachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentEntity {

    @Id
    @Column(name = "id")
    private String uuid;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "file_id")
    private String fileId;

    @Column(name = "file_name")
    private String fileName;
}
