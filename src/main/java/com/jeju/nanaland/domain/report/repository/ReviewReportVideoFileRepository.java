package com.jeju.nanaland.domain.report.repository;

import com.jeju.nanaland.domain.report.entity.review.ReviewReportVideoFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReportVideoFileRepository extends
    JpaRepository<ReviewReportVideoFile, Long> {

}