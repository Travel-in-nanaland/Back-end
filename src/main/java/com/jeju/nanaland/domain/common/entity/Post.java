package com.jeju.nanaland.domain.common.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "DTYPE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Post extends BaseEntity {

  // nanaContent 때문에 nullable 허용, 논의해보기..
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "image_file_id")
  private ImageFile firstImageFile;

  @NotNull
  private Long priority;

  @Column(name = "view_count", nullable = false, columnDefinition = "int default 0")
  private int viewCount;

  protected Post(ImageFile firstImageFile, Long priority) {
    this.firstImageFile = firstImageFile;
    this.priority = priority;
  }
}
