package com.jeju.nanaland.domain.nana.repository;

import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NanaTitleRepository extends JpaRepository<NanaTitle, Long> {

  Optional<NanaTitle> findNanaTitleById(Long id);
}
