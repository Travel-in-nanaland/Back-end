package com.jeju.nanaland.domain.nana.repository;

import com.jeju.nanaland.domain.nana.entity.Nana;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NanaRepository extends JpaRepository<Nana, Long>, NanaRepositoryCustom {

}
