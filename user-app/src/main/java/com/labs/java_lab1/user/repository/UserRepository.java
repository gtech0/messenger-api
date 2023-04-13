package com.labs.java_lab1.user.repository;

import com.labs.java_lab1.user.entity.UserEntity;
import org.springframework.data.domain.Example;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByLogin(String login);
    Optional<UserEntity> getByUuidAndFullName(String id, String name);
    Optional<UserEntity> getByUuid(String id);
    boolean existsByEmail(String email);

    Page<UserEntity> findAll(Example example, Pageable pageable);

}
