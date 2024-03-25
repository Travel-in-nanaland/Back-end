package com.jeju.nanaland.domain.nature.entity;

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
public class Nature extends Common {

  @OneToMany(mappedBy = "nature", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<NatureTrans> natureTrans;
}