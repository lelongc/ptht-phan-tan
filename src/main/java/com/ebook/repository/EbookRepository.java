package com.ebook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ebook.entity.Ebook;
import com.ebook.enums.EbookStatus;

public interface EbookRepository extends JpaRepository<Ebook, Long> {
    Optional<Ebook> findFirstByStatus(EbookStatus status);
}
