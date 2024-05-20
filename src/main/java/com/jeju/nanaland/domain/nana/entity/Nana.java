package com.jeju.nanaland.domain.nana.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import jakarta.persistence.CascadeType;
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
public class Nana extends BaseEntity {

  @NotBlank
  private String version;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "nana_title_image_file_id", nullable = false, unique = true)
  private ImageFile nanaTitleImageFile;

  @OneToMany(mappedBy = "nana", cascade = CascadeType.REMOVE)
  private List<NanaContentImage> nanaContentImageList;

  @Builder
  public Nana(String version, ImageFile nanaTitleImageFile,
      List<NanaContentImage> nanaContentImageList) {
    this.version = version;
    this.nanaTitleImageFile = nanaTitleImageFile;
    this.nanaContentImageList = nanaContentImageList;
  }

  public void updateNanaContentImageList(List<NanaContentImage> nanaContentImageList) {
    this.nanaContentImageList = nanaContentImageList;
  }

}


