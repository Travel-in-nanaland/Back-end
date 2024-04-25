package com.jeju.nanaland.domain.report.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.report.dto.ReportRequest;
import com.jeju.nanaland.domain.report.entity.FixType;
import com.jeju.nanaland.domain.report.entity.InfoFixReport;
import com.jeju.nanaland.domain.report.repository.InfoFixReportRepository;
import com.jeju.nanaland.global.imageUpload.S3ImageService;
import com.sun.jdi.InternalException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

  private final InfoFixReportRepository infoFixReportRepository;
  private final S3ImageService s3ImageService;

  @Transactional
  public void postInfoFixReport(ReportRequest.InfoFixDto reqDto, MultipartFile multipartFile) {

    ImageFile imageFile;
    try {
      imageFile = s3ImageService.uploadAndSaveImage(multipartFile, false);
    } catch (IOException e) {
      throw new InternalException("이미지 업로드 실패");
    }

    if (imageFile == null) {
      throw new InternalException("이미지 업로드 실패");
    }

    CategoryContent categoryContent = CategoryContent.valueOf(reqDto.getCategoryContent());
    FixType fixType = FixType.valueOf(reqDto.getFixType());

    infoFixReportRepository.save(InfoFixReport.builder()
        .category(categoryContent)
        .fixType(fixType)
        .content(reqDto.getContent())
        .email(reqDto.getEmail())
        .imageUrl(imageFile.getOriginUrl())
        .build());
  }
}
