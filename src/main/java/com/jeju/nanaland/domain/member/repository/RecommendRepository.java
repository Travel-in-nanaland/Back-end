package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.member.entity.Recommend;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendRepository extends JpaRepository<Recommend, Long>,
    RecommendRepositoryCustom {

  List<Recommend> findAllByTravelType(TravelType travelType);
}
