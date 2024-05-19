package com.jeju.nanaland.domain.nana.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "nana_content_Image",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "imageFileNanaContentUnique",
            columnNames = {"image_file_id", "nana_id"}
        )
    }
)
public class NanaContentImage extends BaseEntity {

  @ManyToOne
  @NotNull
  @JoinColumn(name = "nana_id")
  private Nana nana;

  @ManyToOne
  @NotNull
  @JoinColumn(name = "image_file_id")
  private ImageFile imageFile;

  @Column(nullable = false)
  private int number;

}
