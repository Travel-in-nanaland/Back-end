package com.jeju.nanaland.domain.nature.entity;

import com.jeju.nanaland.domain.common.entity.Common;
import jakarta.persistence.CascadeType;
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
public class Nature extends Common {

  @OneToMany(mappedBy = "nature", cascade = CascadeType.REMOVE)
  private List<NatureTrans> natureTrans;

  @Builder
  public Nature(String imageUrl, String contact) {
    super(imageUrl, contact);
    this.natureTrans = new ArrayList<>();
  }
}
