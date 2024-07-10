package com.jeju.nanaland.domain.nana.repository;

import com.jeju.nanaland.domain.nana.entity.NanaContent;
import com.jeju.nanaland.domain.nana.entity.NanaTitle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NanaContentRepository extends JpaRepository<NanaContent, Long> {

  List<NanaContent> findAllByNanaTitleOrderByPriority(NanaTitle nanaTitle);

  int countNanaContentByNanaTitle(NanaTitle nanaTitle);
}
