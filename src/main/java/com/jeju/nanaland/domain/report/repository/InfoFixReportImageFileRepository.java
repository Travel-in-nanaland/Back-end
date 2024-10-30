package com.jeju.nanaland.domain.report.repository;

import com.jeju.nanaland.domain.report.entity.infoFix.InfoFixReportImageFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfoFixReportImageFileRepository extends
    JpaRepository<InfoFixReportImageFile, Long> {

}
