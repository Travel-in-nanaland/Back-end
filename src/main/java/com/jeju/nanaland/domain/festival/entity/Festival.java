package com.jeju.nanaland.domain.festival.entity;

import com.jeju.nanaland.domain.common.entity.Common;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Festival extends Common {

  @Column(columnDefinition = "VARCHAR(2048)")
  private String homepage;

  @OneToMany(mappedBy = "festival", cascade = CascadeType.REMOVE)
  private List<FestivalTrans> festivalTrans;

  @Builder
  public Festival(ImageFile imageFile, String contact, String homepage) {
    super(imageFile, contact);
    this.homepage = homepage;
    this.festivalTrans = new ArrayList<>();
  }
}
