package com.jeju.nanaland.domain.restaurant.entity;

import com.jeju.nanaland.domain.common.entity.Post;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("RESTAURANT")
public class Restaurant extends Post {

  private String contact;

  private String homepage;

  private String instagram;

  @OneToMany(mappedBy = "restaurant", cascade = CascadeType.REMOVE)
  private List<RestaurantTrans> restaurantTrans;
}
