package com.jeju.nanaland.domain.report.repository;

import com.jeju.nanaland.domain.report.entity.review.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {

}
