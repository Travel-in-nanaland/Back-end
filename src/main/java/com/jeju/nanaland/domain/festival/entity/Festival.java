package com.jeju.nanaland.domain.festival.entity;

import com.jeju.nanaland.domain.common.entity.Common;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
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

  private LocalDate startDate;
  private LocalDate endDate;

  @Column(columnDefinition = "VARCHAR(2048)")
  private String homepage;

  @OneToMany(mappedBy = "festival", cascade = CascadeType.REMOVE)
  private List<FestivalTrans> festivalTrans;

  @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
  private boolean onGoing;

  @Builder
  public Festival(String contentId, ImageFile imageFile, String contact, LocalDate startDate,
      LocalDate endDate, String homepage, boolean onGoing) {
    super(contentId, imageFile, contact);
    this.startDate = startDate;
    this.endDate = endDate;
    this.homepage = homepage;
    this.festivalTrans = new ArrayList<>();
    this.onGoing = onGoing;
  }
}
