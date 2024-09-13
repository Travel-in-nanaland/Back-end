package com.jeju.nanaland.domain.report.entity;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.report.entity.claim.ClaimReport;
import com.jeju.nanaland.domain.report.entity.claim.ClaimReportImageFile;
import com.jeju.nanaland.domain.report.repository.ClaimReportImageFileRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ClaimReportStrategy implements ReportStrategy {

  private final ClaimReportImageFileRepository claimReportImageFileRepository;

  @Override
  public ReportType getReportType() {
    return ReportType.CLAIM;
  }

  /**
   * 신고 요청 이미지와 Report 매핑 생성 및 저장
   *
   * @param report     신고 요청
   * @param imageFiles 이미지 리스트
   */
  @Override
  public void saveReportImages(Report report, List<ImageFile> imageFiles) {
    List<ClaimReportImageFile> reportImageFiles = imageFiles.stream()
        .map(imageFile -> ClaimReportImageFile.builder()
            .imageFile(imageFile)
            .claimReport((ClaimReport) report)
            .build())
        .toList();
    claimReportImageFileRepository.saveAll(reportImageFiles);
  }
}
