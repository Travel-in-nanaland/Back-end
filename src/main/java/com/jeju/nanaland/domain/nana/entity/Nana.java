package com.jeju.nanaland.domain.nana.entity;

import com.jeju.nanaland.domain.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Nana extends BaseEntity {

  String imageUrl;

  @OneToMany(mappedBy = "nana", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<NanaTrans> nanaTrans;
}