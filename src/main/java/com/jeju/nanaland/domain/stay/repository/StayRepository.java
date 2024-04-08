package com.jeju.nanaland.domain.stay.repository;

import com.jeju.nanaland.domain.stay.entity.Stay;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StayRepository extends JpaRepository<Stay, Long>, StayRepositoryCustom {

}
