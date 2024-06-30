package com.jeju.nanaland.domain.hashtag.entity;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Post;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "hashtag",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "LanguageKeywordCategoryPostUnique",
            columnNames = {"language_id", "keyword_id", "category_id", "post_id"}
        )
    }
)
public class Hashtag extends BaseEntity {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "language_id", nullable = false)
  private Language language;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "keyword_id", nullable = false)
  private Keyword keyword;

  @NotNull
  @Enumerated(EnumType.STRING)
  private Category category;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;
}
