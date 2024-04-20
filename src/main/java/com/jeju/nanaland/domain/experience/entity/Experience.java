package com.jeju.nanaland.domain.experience.entity;

import com.jeju.nanaland.domain.common.entity.Common;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Experience extends Common {

  private String type;

  private Float ratingAvg;

  @OneToMany(mappedBy = "experience", cascade = CascadeType.REMOVE)
  private List<ExperienceTrans> experienceTrans;

  @Builder
  public Experience(String contentId, ImageFile imageFile, String contact, String type,
      Float ratingAvg) {
    super(contentId, imageFile, contact);
    this.type = type;
    this.ratingAvg = ratingAvg;
    this.experienceTrans = new ArrayList<>();
  }
}
