package com.jeju.nanaland.domain.review.repository;

import com.jeju.nanaland.domain.review.entity.Review;
import com.jeju.nanaland.domain.review.entity.ReviewImageFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewImageFileRepository extends JpaRepository<ReviewImageFile, Long> {

  List<ReviewImageFile> findAllByReview(Review review);

  void deleteAllByReview(Review review);
}
