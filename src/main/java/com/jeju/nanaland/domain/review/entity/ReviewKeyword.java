package com.jeju.nanaland.domain.review.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    name = "review_keyword",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "reviewReviewTypeKeywordUnique",
            columnNames = {"review_id", "review_type_keyword"}
        )
    }
)
public class ReviewKeyword extends BaseEntity {

  @ManyToOne
  @NotNull
  @JoinColumn(name = "review_id")
  private Review review;

  @Enumerated(EnumType.STRING)
  private ReviewTypeKeyword reviewTypeKeyword;
}
