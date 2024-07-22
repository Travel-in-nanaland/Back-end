package com.jeju.nanaland.domain.restaurant.dto;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.CompositeDto;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RestaurantCompositeDto extends CompositeDto {

  private String homepage;

  private String instagram;

  private String reservation;

  private String takeOut;

  private String parking;

  private String groupReservation;

  private String pet;

  @QueryProjection
  public RestaurantCompositeDto(Long id, String originUrl, String thumbnailUrl, String contact,
      Language locale, String title, String content,
      String address, String addressTag, String time, String homepage, String instagram,
      String reservation, String takeOut, String parking, String groupReservation, String pet) {
    super(id, originUrl, thumbnailUrl, contact, locale, title, content, address, addressTag, time);
    this.homepage = homepage;
    this.instagram = instagram;
    this.reservation = reservation;
    this.takeOut = takeOut;
    this.parking = parking;
    this.groupReservation = groupReservation;
    this.pet = pet;
  }
}
