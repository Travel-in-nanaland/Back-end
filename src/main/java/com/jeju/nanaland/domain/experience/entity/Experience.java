package com.jeju.nanaland.domain.experience.entity;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.experience.entity.enums.ExperienceType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@DiscriminatorValue("EXPERIENCE")
public class Experience extends Post {

  private String contentId;

  private String contact;

  @Column(columnDefinition = "VARCHAR(2048)")
  private String homepage;

  @Enumerated(EnumType.STRING)
  private ExperienceType experienceType;

  @OneToMany(mappedBy = "experience", cascade = CascadeType.REMOVE)
  private List<ExperienceTrans> experienceTrans;

  @Builder
  public Experience(ImageFile firstImageFile, Long priority, String contentId, String contact,
      String homepage, ExperienceType experienceType) {
    super(firstImageFile, priority);
    this.contentId = contentId;
    this.contact = contact;
    this.homepage = homepage;
    this.experienceType = experienceType;
    this.experienceTrans = new ArrayList<>();
  }
}
