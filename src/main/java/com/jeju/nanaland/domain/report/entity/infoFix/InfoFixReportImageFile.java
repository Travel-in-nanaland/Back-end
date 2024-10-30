package com.jeju.nanaland.domain.report.entity.infoFix;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InfoFixReportImageFile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "image_file_id")
  private ImageFile imageFile;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "info_fix_report_id")
  private InfoFixReport infoFixReport;

  @Builder
  public InfoFixReportImageFile(ImageFile imageFile, InfoFixReport infoFixReport) {
    this.imageFile = imageFile;
    this.infoFixReport = infoFixReport;
  }
}
