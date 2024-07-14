package com.jeju.nanaland.domain.restaurant.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import jakarta.persistence.Column;
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
public class RestaurantTrans extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "restaurant", nullable = false)
  private Restaurant restaurant;

  private String title;

  @Column(columnDefinition = "VARCHAR(1024)")
  private String content;

  private String time;

  private String address;

  private String reservation;

  private String takeOut;

  private String parking;

  private String group;

  private String pet;

  @Builder
  public RestaurantTrans(Restaurant restaurant, String title, String content, String time,
      String address, String reservation, String takeOut, String parking, String group,
      String pet) {
    this.restaurant = restaurant;
    this.title = title;
    this.content = content;
    this.time = time;
    this.address = address;
    this.reservation = reservation;
    this.takeOut = takeOut;
    this.parking = parking;
    this.group = group;
    this.pet = pet;
  }
}
