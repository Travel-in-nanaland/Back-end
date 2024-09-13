package com.jeju.nanaland.domain.report.entity;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import java.util.List;

public interface ReportStrategy {
  ReportType getReportType();
  void saveReportImages(Report report, List<ImageFile> saveImageFiles);
}
