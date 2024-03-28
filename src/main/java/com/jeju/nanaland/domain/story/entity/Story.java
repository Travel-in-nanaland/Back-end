package com.jeju.nanaland.domain.story.entity;

import com.jeju.nanaland.domain.comment.entity.Comment;
import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.member.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Story extends BaseEntity {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private StoryCategory storyCategory;

  @NotBlank
  @Column(nullable = false)
  private String title;

  @NotBlank
  @Column(nullable = false)
  private String content;

  @OneToMany(mappedBy = "story", cascade = CascadeType.REMOVE)
  private List<StoryImageFile> storyImageFiles;

  @OneToMany(mappedBy = "story", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<Comment> comments;

  @Builder
  public Story(Member member, StoryCategory storyCategory, String title, String content,
      List<StoryImageFile> storyImageFiles) {
    this.member = member;
    this.storyCategory = storyCategory;
    this.title = title;
    this.content = content;
    this.storyImageFiles = (storyImageFiles != null) ? storyImageFiles : new ArrayList<>();
    this.comments = new ArrayList<>();
  }
}
