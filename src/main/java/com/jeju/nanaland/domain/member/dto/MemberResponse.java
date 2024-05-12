package com.jeju.nanaland.domain.member.dto;

import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.member.entity.Member;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
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

  @Data
  @Builder
  @Schema(description = "사용자 정보 조회 DTO")
  public static class ProfileDto {

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "회원가입 형태", example = "KAKAO, GOOGLE, APPLE")
    private String provider;

    @Schema(description = "프로필 사진 url")
    private String profileImageUrl;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "설명")
    private String description;

    @Schema(description = "레벨")
    private Integer level;

    @Schema(description = "타입")
    private String travelType;

    @Schema(description = "해시태그 리스트")
    private List<String> hashtags;
  }
}
