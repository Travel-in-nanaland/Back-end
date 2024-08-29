package com.jeju.nanaland.domain.experience.repository;

import com.jeju.nanaland.domain.experience.entity.Experience;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceRepository extends JpaRepository<Experience, Long>,
    ExperienceRepositoryCustom {

  List<Long> findAllIds();
}
