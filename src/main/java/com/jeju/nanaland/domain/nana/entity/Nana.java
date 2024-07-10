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

  @Builder
  public Nana(ImageFile firstImageFile, Long priority, String version) {
    super(firstImageFile, priority);
    this.version = version;
  }


}


