package com.jeju.nanaland.domain.nature.repository;

import com.jeju.nanaland.domain.nature.entity.Nature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NatureRepository extends JpaRepository<Nature, Long>, NatureRepositoryCustom {

}
