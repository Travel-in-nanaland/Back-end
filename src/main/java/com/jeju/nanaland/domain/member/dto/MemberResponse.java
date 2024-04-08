package com.jeju.nanaland.domain.member.dto;

import lombok.Builder;
import lombok.Data;

public class MemberResponse {

  @Data
  @Builder
  public static class RecommendedPostsDto {

    private Long id;
    private String category;
    private String thumbnailUrl;
    private String title;
    private String intro;
  }
}
