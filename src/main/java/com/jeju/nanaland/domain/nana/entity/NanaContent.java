package com.jeju.nanaland.domain.nana.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NanaContent extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "nana_title_id", nullable = false)
  private NanaTitle nanaTitle;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "image_file_id", nullable = false)
  private ImageFile imageFile;

  @Column(nullable = false)
  private int number;

  @NotBlank
  @Column(nullable = false)
  private String subTitle;

  @NotBlank
  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String content;

  @ManyToMany
  @JoinTable(name = "NANA_INFO_TYPE",
      joinColumns = @JoinColumn(name = "nana_content_id"),
      inverseJoinColumns = @JoinColumn(name = "nana_additional_info_id")
  )
  private Set<NanaAdditionalInfo> infoList;

  @Builder
  public NanaContent(NanaTitle nanaTitle, ImageFile imageFile, int number, String subTitle,
      String title, String content, Set<NanaAdditionalInfo> infoList) {
    this.nanaTitle = nanaTitle;
    this.imageFile = imageFile;
    this.number = number;
    this.subTitle = subTitle;
    this.title = title;
    this.content = content;
    this.infoList = infoList;
  }
}
