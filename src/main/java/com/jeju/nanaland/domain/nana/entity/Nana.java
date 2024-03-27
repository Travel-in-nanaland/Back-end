package com.jeju.nanaland.domain.nana.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Nana extends BaseEntity {

  @Column(columnDefinition = "VARCHAR(2048)")
  private String imageUrl;

  @OneToMany(mappedBy = "nana", cascade = CascadeType.REMOVE)
  private List<NanaTrans> nanaTrans;

  @Builder
  public Nana(String imageUrl) {
    this.imageUrl = imageUrl;
    this.nanaTrans = new ArrayList<>();
  }
}
