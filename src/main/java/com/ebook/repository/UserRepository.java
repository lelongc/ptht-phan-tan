package com.ebook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ebook.entity.User;
import com.ebook.enums.UserStatus;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("select u from User u where u.status = :status")
    Optional<User> findFirstByStatus(@Param("status") UserStatus status);
}
