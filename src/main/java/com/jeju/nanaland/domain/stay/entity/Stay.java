package com.jeju.nanaland.domain.stay.entity;

import com.jeju.nanaland.domain.common.entity.Common;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
public class Stay extends Common {

  private Integer price;

  @Column(columnDefinition = "VARCHAR(2048)")
  private String homepage;

  private String parking;

  private Float ratingAvg;

  @OneToMany(mappedBy = "stay", cascade = CascadeType.REMOVE)
  private List<StayTrans> stayTrans;

  @Builder
  public Stay(String imageUrl, String contact, Integer price, String homepage, String parking,
      Float ratingAvg) {
    super(imageUrl, contact);
    this.price = price;
    this.homepage = homepage;
    this.parking = parking;
    this.ratingAvg = ratingAvg;
    this.stayTrans = new ArrayList<>();
  }
}
