package com.jeju.nanaland.domain.member.repository;

import com.jeju.nanaland.domain.member.entity.MemberTravelType;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTravelTypeRepository extends JpaRepository<MemberTravelType, Long> {

  MemberTravelType findByTravelType(TravelType travelType);
}
