package com.jeju.nanaland.domain.experience.entity;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
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
@DiscriminatorValue("EXPERIENCE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Experience extends Post {

  private String contentId;

  private String contact;

  private String type;

  private Float ratingAvg;

  @OneToMany(mappedBy = "experience", cascade = CascadeType.REMOVE)
  private List<ExperienceTrans> experienceTrans;

  @Builder
  public Experience(ImageFile imageFile, String contentId, String contact, String type,
      Float ratingAvg) {
    super(imageFile);
    this.contentId = contentId;
    this.contact = contact;
    this.type = type;
    this.ratingAvg = ratingAvg;
    this.experienceTrans = new ArrayList<>();
  }
}
