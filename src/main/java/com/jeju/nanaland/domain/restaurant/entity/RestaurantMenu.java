package com.jeju.nanaland.domain.restaurant.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantMenu extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "restaurant_trans_id", nullable = false)
  private RestaurantTrans restaurantTrans;

  private String menuName;

  private String price;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "image_file_id", nullable = true)
  private ImageFile firstImageFile;
}
