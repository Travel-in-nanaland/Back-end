package com.jeju.nanaland.domain.common.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public class Common extends BaseEntity {

  private String imageUrl;

  private String contact;
}
