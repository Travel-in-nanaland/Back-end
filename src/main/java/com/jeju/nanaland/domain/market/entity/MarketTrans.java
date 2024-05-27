package com.jeju.nanaland.domain.market.entity;

import com.jeju.nanaland.domain.common.entity.CommonTrans;
import com.jeju.nanaland.domain.common.entity.Language;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketTrans extends CommonTrans {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "market_id", nullable = false)
  private Market market;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "language_id", nullable = false)
  private Language language;

  private String intro;

  private String amenity;

  @Builder
  public MarketTrans(Market market, Language language, String title, String content, String address,
      String addressTag, String time, String intro, String amenity) {
    super(title, content, address, addressTag, time);
    this.market = market;
    this.language = language;
    this.intro = intro;
    this.amenity = amenity;
  }
}
