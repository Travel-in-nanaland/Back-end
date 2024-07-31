package com.jeju.nanaland.domain.restaurant.entity;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantTrans extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "restaurant_id", nullable = false)
  private Restaurant restaurant;

  @NotNull
  @Enumerated(EnumType.STRING)
  private Language language;

  private String title;

  @Column(columnDefinition = "VARCHAR(1024)")
  private String content;

  private String time;

  private String address;

  private String addressTag;

  private String service;

  @Builder
  public RestaurantTrans(Restaurant restaurant, Language language, String title, String content,
      String time, String address, String addressTag, String service) {
    this.restaurant = restaurant;
    this.language = language;
    this.title = title;
    this.content = content;
    this.time = time;
    this.address = address;
    this.addressTag = addressTag;
    this.service = service;
  }
}
