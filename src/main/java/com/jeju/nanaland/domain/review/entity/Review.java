package com.jeju.nanaland.domain.review.entity;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @NotNull
  @Enumerated(EnumType.STRING)
  private Category category;

  @NotNull
  @Column(nullable = false)
  private Long postId;

  @NotBlank
  @Column(nullable = false)
  private String title;

  private String content;

  @NotNull
  @Column(nullable = false)
  private Float rating;

  @Builder
  public Review(Member member, Category category, Long postId, String title,
      String content, Float rating) {
    this.member = member;
    this.category = category;
    this.postId = postId;
    this.title = title;
    this.content = (content != null) ? content : "";
    this.rating = (rating != null) ? rating : 0F;
  }
}
