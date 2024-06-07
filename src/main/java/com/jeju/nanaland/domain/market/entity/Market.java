package com.jeju.nanaland.domain.market.entity;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
@DiscriminatorValue("MARKET")
public class Market extends Post {

  private String contentId;

  private String contact;

  @Column(columnDefinition = "VARCHAR(2048)")
  private String homepage;

  @OneToMany(mappedBy = "market", cascade = CascadeType.REMOVE)
  private List<MarketTrans> marketTrans;

  @Builder
  public Market(ImageFile imageFile, String contentId, String contact, String homepage) {
    super(imageFile);
    this.contentId = contentId;
    this.contact = contact;
    this.homepage = homepage;
    this.marketTrans = new ArrayList<>();
  }
}
