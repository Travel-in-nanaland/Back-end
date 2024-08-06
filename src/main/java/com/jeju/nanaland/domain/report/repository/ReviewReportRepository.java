package com.jeju.nanaland.domain.report.repository;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.report.entity.review.ReviewReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {

  Optional<ReviewReport> findByMemberAndReviewId(Member member, Long reviewId);
}
