package com.jeju.nanaland.domain.nana.repository;

import com.jeju.nanaland.domain.nana.entity.Nana;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface NanaRepository extends JpaRepository<Nana, Long>, NanaRepositoryCustom {

  Optional<Nana> findNanaById(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT n FROM Nana n WHERE n.id = :id")
  Optional<Nana> findNanaByIdWithPessimisticLock(@Param("id") Long id);


}
