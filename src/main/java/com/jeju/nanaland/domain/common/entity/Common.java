package com.jeju.nanaland.domain.common.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public class Common {

  private String imageUrl;
  private String contact;
}
