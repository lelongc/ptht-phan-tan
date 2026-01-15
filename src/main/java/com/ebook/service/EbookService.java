package com.ebook.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ebook.dto.EbookResponse;
import com.ebook.entity.Ebook;
import com.ebook.enums.EbookStatus;
import com.ebook.repository.EbookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EbookService {

    private final EbookRepository ebookRepository;

    @Transactional(readOnly = true)
    public EbookResponse getActiveEbook(Locale locale) {
        Ebook ebook = ebookRepository.findFirstByStatus(EbookStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active ebook found. Please seed database."));
        return new EbookResponse(
                ebook.getId(),
                ebook.getTitle(locale),
                ebook.getAuthor(locale),
                ebook.getDescription(locale),
                ebook.getPrice(),
                ebook.getCoverUrl()
        );
    }

    @Transactional(readOnly = true)
    public Ebook getById(Long id) {
        return ebookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ebook not found"));
    }
}
