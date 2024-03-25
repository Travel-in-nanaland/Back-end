package com.jeju.nanaland.domain.festival.entity;

import com.jeju.nanaland.domain.common.entity.Common;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Festival extends Common {

  private String homepage;

  @OneToMany(mappedBy = "festival", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<FestivalTrans> festivalTrans;
}
