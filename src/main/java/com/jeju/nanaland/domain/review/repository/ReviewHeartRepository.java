package com.jeju.nanaland.domain.review.repository;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.review.entity.Review;
import com.jeju.nanaland.domain.review.entity.ReviewHeart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewHeartRepository extends JpaRepository<ReviewHeart, Long> {

  Optional<ReviewHeart> findByMemberAndReview(Member member, Review review);
}
