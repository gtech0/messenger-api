package com.labs.java_lab1.user.repository;

import com.labs.java_lab1.user.dto.UserDto;
import com.labs.java_lab1.user.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByLogin(String login);
    Optional<UserEntity> getByUuidAndFullName(String id, String name);
    Optional<UserEntity> getByUuid(String id);
    boolean existsByEmail(String email);
    @Query(value =
            "SELECT new com.labs.java_lab1.user.dto.UserDto(" +
                    "u.login, u.email, u.fullName, u.birthDate, u.phoneNumber, u.city, u.avatar) " +
                    "FROM UserEntity u " +
                    "WHERE (u.login LIKE CONCAT('%', :login, '%') OR :login IS NULL) " +
                    "AND (u.fullName LIKE CONCAT('%', :fullName, '%') OR :fullName IS NULL) " +
                    "AND (u.birthDate = :birthDate OR cast(:birthDate as date) IS NULL) " +
                    "AND (u.phoneNumber LIKE CONCAT('%', :phoneNumber, '%') OR :phoneNumber IS NULL) " +
                    "AND (u.city LIKE CONCAT('%', :city, '%') OR :city IS NULL)")
    List<UserDto> findAllQuery(String login,
                               String fullName,
                               Date birthDate,
                               String phoneNumber,
                               String city,
                               Pageable pageable);
}
