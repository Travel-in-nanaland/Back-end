package com.jeju.nanaland.domain.restaurant.repository;

import com.jeju.nanaland.domain.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long>,
    RestaurantRepositoryCustom {

}
