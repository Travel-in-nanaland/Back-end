package com.jeju.nanaland.domain.story.entity;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
public class StoryImageFile {

  @Id
  @OneToOne(mappedBy = "image_file_id", cascade = CascadeType.REMOVE)
  @Column(nullable = false)
  private ImageFile imageFile;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "story_id", nullable = false)
  private Story story;
}
