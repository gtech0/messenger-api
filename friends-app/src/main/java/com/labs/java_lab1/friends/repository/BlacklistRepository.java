package com.labs.java_lab1.friends.repository;

import com.labs.java_lab1.friends.entity.BlacklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistRepository extends JpaRepository<BlacklistEntity, String> {
}
