package com.jeju.nanaland.domain.member.dto;

import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.member.entity.Member;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

public class MemberResponse {

  @Data
  @Builder
  public static class MemberInfoDto {

    private Member member;
    private Language language;

    @QueryProjection
    public MemberInfoDto(Member member, Language language) {
      this.member = member;
      this.language = language;
    }
  }

  @Data
  @Builder
  @Schema(description = "타입에 따른 추천 게시물 응답 DTO")
  public static class RecommendPostDto {

    @Schema(description = "게시물 id", example = "1")
    private Long id;

    @Schema(
        description = "게시물 카테고리",
        example = "NATURE",
        allowableValues = {
            "NATURE", "EXPERIENCE", "FESTIVAL", "MARKET"
        })
    private String category;

    @Schema(description = "게시물 썸네일 이미지 URL")
    private String thumbnailUrl;

    @Schema(description = "제목", example = "성산일출봉")
    private String title;

    @Schema(description = "게시물 짧은 설명", example = "성산일출봉은 ...")
    private String intro;
  }
}
