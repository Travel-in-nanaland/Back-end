package com.jeju.nanaland.domain.nana.entity;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("NANA")
public class Nana extends Post {

  @NotBlank
  private String version;

  // TODO: 이 부분을 firstImageFile로 대체 필요
//  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
//  @JoinColumn(name = "nana_title_image_file_id", nullable = false, unique = true)
//  private ImageFile nanaTitleImageFile;

  /**
   * PostImageFile로 대체
   */
//  @OneToMany(mappedBy = "nana", cascade = CascadeType.REMOVE)
//  private List<NanaContentImage> nanaContentImageList;
  @Builder
  public Nana(ImageFile firstImageFile, Long priority, String version) {
    super(firstImageFile, priority);
    this.version = version;
  }

//  public void updateNanaContentImageList(List<NanaContentImage> nanaContentImageList) {
//    this.nanaContentImageList = nanaContentImageList;
//  }

}


