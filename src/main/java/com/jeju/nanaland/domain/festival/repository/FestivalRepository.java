package com.jeju.nanaland.domain.festival.repository;

import com.jeju.nanaland.domain.festival.entity.Festival;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalRepository extends JpaRepository<Festival, Long>,
    FestivalRepositoryCustom {

  List<Festival> findAllByOnGoingAndEndDateBefore(boolean onGoing, LocalDate localDate);

  List<Festival> findAllByEndDateBefore(LocalDate localDate);
}
