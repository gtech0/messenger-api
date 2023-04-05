package com.labs.java_lab1.friends.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "blacklist")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistEntity {
    @Id
    @Column(name = "id")
    private String uuid;

    @Column
    private String userId;

    @Column
    private String friendId;

    @Column(name = "friend_name")
    private String friendName;
}
