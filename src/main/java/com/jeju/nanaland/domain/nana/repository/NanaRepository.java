package com.jeju.nanaland.domain.nana.repository;

import com.jeju.nanaland.domain.nana.entity.Nana;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NanaRepository extends JpaRepository<Nana, Long>, NanaRepositoryCustom {

  Optional<Nana> findNanaById(Long id);
}
