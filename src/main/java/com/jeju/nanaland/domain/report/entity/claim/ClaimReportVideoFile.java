package com.jeju.nanaland.domain.report.entity.claim;

import com.jeju.nanaland.domain.common.entity.VideoFile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClaimReportVideoFile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "video_file_id", nullable = false)
  private VideoFile videoFile;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "claim_report_id", nullable = false)
  private ClaimReport claimReport;

  @Builder
  public ClaimReportVideoFile(VideoFile videoFile, ClaimReport claimReport) {
    this.videoFile = videoFile;
    this.claimReport = claimReport;
  }
}
