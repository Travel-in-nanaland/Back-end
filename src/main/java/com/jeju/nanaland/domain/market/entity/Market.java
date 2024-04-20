package com.jeju.nanaland.domain.market.entity;

import com.jeju.nanaland.domain.common.entity.Common;
import com.jeju.nanaland.domain.common.entity.ImageFile;
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
public class Market extends Common {

  @Column(columnDefinition = "VARCHAR(2048)")
  private String homepage;

  @OneToMany(mappedBy = "market", cascade = CascadeType.REMOVE)
  private List<MarketTrans> marketTrans;

  @Builder
  public Market(String contentId, ImageFile imageFile, String contact, String homepage) {
    super(contentId, imageFile, contact);
    this.homepage = homepage;
    this.marketTrans = new ArrayList<>();
  }
}
