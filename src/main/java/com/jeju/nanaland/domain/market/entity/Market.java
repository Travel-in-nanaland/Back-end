package com.jeju.nanaland.domain.market.entity;

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
public class Market extends Common {

  private String homepage;

  @OneToMany(mappedBy = "market", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<MarketTrans> marketTrans;

  @Builder
  public Market(String imageUrl, String contact, String homepage) {
    super(imageUrl, contact);
    this.homepage = homepage;
  }
}
