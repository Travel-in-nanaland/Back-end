package com.jeju.nanaland.domain.festival.entity;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Status;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@AllArgsConstructor
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Festival extends Common {

  @Enumerated(value = EnumType.STRING)
  @Column(name = "status")
  private Status status = Status.ACTIVE;

  private LocalDate startDate;
  private LocalDate endDate;
  private String season;

  @Column(columnDefinition = "VARCHAR(2048)")
  private String homepage;

  @OneToMany(mappedBy = "festival", cascade = CascadeType.REMOVE)
  private List<FestivalTrans> festivalTrans;

  @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
  private boolean onGoing;

  @Builder
  public Festival(String contentId, ImageFile imageFile, String contact, LocalDate startDate,
      LocalDate endDate, String season, String homepage, boolean onGoing) {
    super(contentId, imageFile, contact);
    this.startDate = startDate;
    this.endDate = endDate;
    this.season = season;
    this.homepage = homepage;
    this.festivalTrans = new ArrayList<>();
    this.onGoing = onGoing;
  }

  public void updateOnGoing(boolean onGoing) {
    this.onGoing = onGoing;
  }

  public void updateStatus(Status status) {
    this.status = status;
  }
}
