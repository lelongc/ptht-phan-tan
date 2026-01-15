package com.ebook.entity;

import java.math.BigDecimal;
import java.util.Locale;

import com.ebook.enums.EbookStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ebooks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ebook extends BaseEntity {

    @NotBlank
    private String titleVi;

    @NotBlank
    private String titleEn;

    private String authorVi;

    private String authorEn;

    @Lob
    private String descriptionVi;

    @Lob
    private String descriptionEn;

    @NotNull
    @Positive
    private BigDecimal price;

    private String coverUrl;

    @NotBlank
    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "file_size_mb")
    private BigDecimal fileSizeMb;

    @Column(name = "page_count")
    private Integer pageCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EbookStatus status = EbookStatus.ACTIVE;

    public String getTitle(Locale locale) {
        return locale != null && "en".equalsIgnoreCase(locale.getLanguage()) ? titleEn : titleVi;
    }

    public String getAuthor(Locale locale) {
        return locale != null && "en".equalsIgnoreCase(locale.getLanguage()) ? authorEn : authorVi;
    }

    public String getDescription(Locale locale) {
        return locale != null && "en".equalsIgnoreCase(locale.getLanguage()) ? descriptionEn : descriptionVi;
    }
}
