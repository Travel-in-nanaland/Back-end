package com.jeju.nanaland.domain.experience.entity;

import com.jeju.nanaland.domain.common.entity.Common;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Experience extends Common {

  private String rating;

  @OneToMany(mappedBy = "experience", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<ExperienceTrans> experienceTrans;

  @Builder
  public Experience(String imageUrl, String contact, String rating) {
    super(imageUrl, contact);
    this.rating = rating;
  }
}
