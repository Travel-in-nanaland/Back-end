package com.jeju.nanaland.domain.stay.entity;

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
public class Stay extends Common {

  private Integer price;

  private String homepage;

  private String parking;

  private Integer rating;

  @OneToMany(mappedBy = "stay", cascade = CascadeType.REMOVE)
  private List<StayTrans> stayTrans;
}
