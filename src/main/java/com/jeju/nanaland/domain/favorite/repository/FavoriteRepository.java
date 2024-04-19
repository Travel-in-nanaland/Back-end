package com.jeju.nanaland.domain.favorite.repository;

import com.jeju.nanaland.domain.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

}
