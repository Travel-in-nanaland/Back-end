package com.jeju.nanaland.domain.nature.entity;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("NATURE")
public class Nature extends Post {

  private String contentId;

  private String contact;

  @OneToMany(mappedBy = "nature", cascade = CascadeType.REMOVE)
  private List<NatureTrans> natureTrans;

  @Builder
  public Nature(ImageFile firstImageFile, Long priority, String contentId, String contact) {
    super(firstImageFile, priority);
    this.contentId = contentId;
    this.contact = contact;
    this.natureTrans = new ArrayList<>();
  }
}
