package com.jeju.nanaland.domain.restaurant.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.restaurant.entity.enums.RestaurantTypeKeyword;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "restaurant_keyword",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "restaurantTypeKeywordUnique",
            columnNames = {"restaurant_id", "restaurant_type_keyword"}
        )
    }
)
public class RestaurantKeyword extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  private Restaurant restaurant;

  @Enumerated(EnumType.STRING)
  private RestaurantTypeKeyword restaurantTypeKeyword;
}
