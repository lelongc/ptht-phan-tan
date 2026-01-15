package com.ebook.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ebook.entity.DownloadToken;

public interface DownloadTokenRepository extends JpaRepository<DownloadToken, Long> {
    Optional<DownloadToken> findByToken(String token);

    @Query("select t from DownloadToken t where t.expiresAt < :now")
    List<DownloadToken> findExpired(@Param("now") Instant now);
}
