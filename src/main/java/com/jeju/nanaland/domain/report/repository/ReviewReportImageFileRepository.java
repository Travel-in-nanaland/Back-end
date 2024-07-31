package com.jeju.nanaland.domain.report.repository;

import com.jeju.nanaland.domain.report.entity.review.ReviewReportImageFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReportImageFileRepository extends
    JpaRepository<ReviewReportImageFile, Long> {

}