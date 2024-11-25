package com.jeju.nanaland.domain.festival.repository;

import com.jeju.nanaland.domain.festival.entity.Festival;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FestivalRepository extends JpaRepository<Festival, Long>,
    FestivalRepositoryCustom {

  List<Festival> findAllByOnGoingAndEndDateBefore(boolean onGoing, LocalDate localDate);

  List<Festival> findAllByEndDateBefore(LocalDate localDate);

  @Query("SELECT f.onGoing FROM Festival f WHERE f.id = :id")
  Boolean getFestivalOnGoingStatusById(@Param("id") Long id);
}
