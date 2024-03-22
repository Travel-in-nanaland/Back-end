package com.jeju.nanaland.domain.common.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public class CommonTrans extends BaseEntity {

  private String title;
  private String content;
  private String address;
  private String time;
}
