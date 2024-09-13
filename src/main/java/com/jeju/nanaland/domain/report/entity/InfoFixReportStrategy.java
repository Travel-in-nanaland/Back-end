package com.jeju.nanaland.domain.report.entity;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.report.repository.InfoFixReportImageFileRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class InfoFixReportStrategy implements ReportStrategy {

  private final InfoFixReportImageFileRepository infoFixReportImageFileRepository;

  @Override
  public ReportType getReportType() {
    return ReportType.INFO_FIX;
  }

  /**
   * 정보수정 제안 이미지와 Report 매핑 생성 및 저장
   *
   * @param report     정보수정 제안 요청
   * @param imageFiles 이미지 리스트
   */
  @Override
  public void saveReportImages(Report report, List<ImageFile> imageFiles) {
    List<InfoFixReportImageFile> reportImageFiles = imageFiles.stream()
        .map(imageFile -> InfoFixReportImageFile.builder()
            .imageFile(imageFile)
            .infoFixReport((InfoFixReport) report)
            .build())
        .toList();
    infoFixReportImageFileRepository.saveAll(reportImageFiles);
  }
}
