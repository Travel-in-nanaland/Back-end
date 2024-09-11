package com.jeju.nanaland.domain.report.repository;

import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.report.entity.claim.ClaimReport;
import com.jeju.nanaland.domain.report.entity.claim.ReportType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimReportRepository extends JpaRepository<ClaimReport, Long> {

  Optional<ClaimReport> findByMemberAndReferenceIdAndReportType(Member member, Long referenceId,
      ReportType reportType);
}
