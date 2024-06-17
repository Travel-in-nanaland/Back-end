package com.jeju.nanaland.domain.nana.entity;

import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
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
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "nana_title_image_file_id", nullable = false, unique = true)
  private ImageFile nanaTitleImageFile;

  @OneToMany(mappedBy = "nana", cascade = CascadeType.REMOVE)
  private List<NanaContentImage> nanaContentImageList;

  @Builder
  public Nana(ImageFile firstImageFile, Long priority, String version, ImageFile nanaTitleImageFile,
      List<NanaContentImage> nanaContentImageList) {
    super(firstImageFile, priority);
    this.version = version;
    this.nanaTitleImageFile = nanaTitleImageFile;
    this.nanaContentImageList = nanaContentImageList;
  }

  public void updateNanaContentImageList(List<NanaContentImage> nanaContentImageList) {
    this.nanaContentImageList = nanaContentImageList;
  }

}


