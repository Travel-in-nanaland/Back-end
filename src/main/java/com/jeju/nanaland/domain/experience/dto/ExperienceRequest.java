package com.jeju.nanaland.domain.experience.dto;

import lombok.Data;

public class ExperienceRequest {

  @Data
  public static class LikeDto {

    private Long postId;

  }
}
