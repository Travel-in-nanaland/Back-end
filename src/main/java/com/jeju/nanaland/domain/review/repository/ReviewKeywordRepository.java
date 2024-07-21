package com.jeju.nanaland.domain.review.repository;

import com.jeju.nanaland.domain.review.entity.ReviewKeyword;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewKeywordRepository extends JpaRepository<ReviewKeyword, Long> {

  List<ReviewKeyword> findAllById(Long id);
}
