package com.jeju.nanaland.domain.restaurant.entity;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("RESTAURANT")
public class Restaurant extends Post {

  @Column(unique = true)
  private String contentId;

  private String contact;

  private String homepage;

  private String instagram;

  @OneToMany(mappedBy = "restaurant", cascade = CascadeType.REMOVE)
  private List<RestaurantTrans> restaurantTrans;

  @Builder
  public Restaurant(ImageFile firstImageFile, Long priority, String contentId, String contact,
      String homepage, String instagram) {
    super(firstImageFile, priority);
    this.contentId = contentId;
    this.contact = contact;
    this.homepage = homepage;
    this.instagram = instagram;
  }
}
