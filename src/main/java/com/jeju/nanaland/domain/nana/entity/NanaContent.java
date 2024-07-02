package com.jeju.nanaland.domain.nana.entity;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
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
@DiscriminatorValue("NANA_CONTENT")
public class NanaContent extends Post {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "nana_title_id", nullable = false)
  private NanaTitle nanaTitle;

  /**
   * Post의 priority로 대체
   */
//  @Column(nullable = false)
//  private int number;

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
  public NanaContent(ImageFile firstImageFile, Long priority, NanaTitle nanaTitle, String subTitle,
      String title, String content, Set<NanaAdditionalInfo> infoList) {
    super(firstImageFile, priority);
    this.nanaTitle = nanaTitle;
    this.subTitle = subTitle;
    this.title = title;
    this.content = content;
    this.infoList = infoList;
  }
}
