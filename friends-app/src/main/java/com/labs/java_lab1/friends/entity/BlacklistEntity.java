package com.labs.java_lab1.friends.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "blacklist",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "friend_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistEntity {

    @Id
    @Column(name = "id")
    private String uuid;

    @Temporal(TemporalType.DATE)
    @Column(name = "add_date")
    private Date addDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "delete_date")
    private Date deleteDate;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "friend_id")
    private String friendId;

    @Column(name = "friend_name")
    private String friendName;
}
