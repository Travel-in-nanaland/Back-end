package com.jeju.nanaland.domain.report.repository;

import com.jeju.nanaland.domain.report.entity.claim.ClaimReportImageFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimReportImageFileRepository extends
    JpaRepository<ClaimReportImageFile, Long> {

}