package com.jeju.nanaland.domain.member.dto;

import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class MemberResponse {

  @Data
  @Builder
  @Schema(description = "회원 정보 DTO")
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
  @AllArgsConstructor
  @Schema(description = "타입에 따른 추천 게시물 응답 DTO")
  public static class RecommendPostDto {

    @Schema(description = "게시물 id", example = "1")
    private Long id;

    @Schema(
        description = "게시물 카테고리",
        example = "NATURE",
        allowableValues = {
            "NATURE", "ACTIVITY", "CULTURE_AND_ARTS", "FESTIVAL", "MARKET", "RESTAURANT"
        })
    private String category;

    @Schema(description = "게시물 썸네일 이미지")
    private ImageFileDto firstImage;

    @Schema(description = "제목", example = "성산일출봉")
    private String title;

    @Schema(description = "게시물 짧은 설명", example = "성산일출봉은 ...")
    private String introduction;

    @Schema(description = "좋아요 여부")
    private boolean isFavorite;

    @QueryProjection
    public RecommendPostDto(Long id, String category, String originUrl,
        String thumbnailUrl, String title, String introduction) {
      this.id = id;
      this.category = category;
      this.firstImage = new ImageFileDto(originUrl, thumbnailUrl);
      this.title = title;
      this.introduction = introduction;
      this.isFavorite = false;
    }
  }

  @Data
  @Builder
  @Schema(description = "사용자 정보 조회 DTO")
  public static class ProfileDto {

    @Schema(description = "이용약관 동의 여부 - 내 프로필인 경우에만 제공")
    List<ConsentItemDto> consentItems;
    @Schema(description = "내 프로필인지 확인 여부")
    private boolean isMyProfile;
    @Schema(description = "유저 Id")
    private Long memberId;
    @Schema(description = "이메일")
    private String email;
    @Schema(description = "회원가입 형태", example = "KAKAO, GOOGLE, APPLE")
    private String provider;
    @Schema(description = "프로필 사진 url")
    private ImageFileDto profileImage;
    @Schema(description = "닉네임")
    private String nickname;
    @Schema(description = "설명")
    private String description;
    @Schema(description = "타입")
    private String travelType;
    @Schema(description = "타입 키")
    private String travelTypeKey;
    @Schema(description = "해시태그 리스트")
    private List<String> hashtags;
  }

  @Data
  @Builder
  @Schema(name = "ConsentItemResponse", description = "이용약관 동의 여부")
  public static class ConsentItemDto {

    @Schema(description = "이용약관", example = "TERMS_OF_USE",
        allowableValues = {"MARKETING", "LOCATION_SERVICE"})
    private String consentType;

    @Schema(description = "동의 여부", defaultValue = "false")
    private Boolean consent;
  }
}
