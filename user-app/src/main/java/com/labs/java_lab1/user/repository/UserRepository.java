package com.labs.java_lab1.user.repository;

import com.labs.java_lab1.user.entity.UserEntity;
import org.springframework.data.domain.Example;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    UserEntity findByLogin(String login);
    boolean existsByLogin(String login);
    boolean existsByEmail(String email);

    Page<UserEntity> findAll(Example example, Pageable pageable);
}
