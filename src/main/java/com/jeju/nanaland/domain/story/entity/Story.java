package com.jeju.nanaland.domain.story.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Story extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @Enumerated(EnumType.STRING)
  private StoryCategory storyCategory;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String content;

  @ElementCollection
  private List<String> imageUrls;
}