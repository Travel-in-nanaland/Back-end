package com.jeju.nanaland.domain.festival.repository;

import com.jeju.nanaland.domain.festival.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FestivalRepository extends JpaRepository<Festival, Long>,
    FestivalRepositoryCustom {

}
