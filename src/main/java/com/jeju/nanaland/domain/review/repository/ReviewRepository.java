package com.jeju.nanaland.domain.review.repository;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.review.entity.Review;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {

  Optional<Review> findReviewByIdAndMember(Long id, Member member);
}