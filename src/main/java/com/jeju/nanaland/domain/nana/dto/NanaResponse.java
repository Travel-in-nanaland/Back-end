package com.jeju.nanaland.domain.nana.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class NanaResponse {

  @Data
  @Builder
  @Schema(description = "나나's pick 전체 썸네일 조회 DTO")
  public static class NanaThumbnailDto {

    @Schema(description = "총 조회 개수")
    private Long totalElements;

    @Schema(description = "결과 데이터")
    private List<NanaThumbnail> data;


  }

  @Data
  @Builder
  @AllArgsConstructor
  @Schema(description = "나나's pick 개별 썸네일 조회 DTO")
  public static class NanaThumbnail {

    @Schema(description = "게시물 id")
    private Long id;

    @Schema(description = "게시물 썸네일 이미지")
    private ImageFileDto firstImage;

    @Schema(description = "오른 쪽 위 버전 ex) nana's Pick vol.1")
    private String version;

    @Schema(description = "사진에 들어갈 부제목 ex) 제주 야경 봤슴무언?")
    private String subHeading;

    @Schema(description = "사진에 들어갈 제목 ex) TOP 10 야경 맛집")
    private String heading;

    @Schema(description = "게시물에 new tag 유무 / true 일 경우에 new 태그")
    private boolean newest;

    // querydsl에서만 사용, 클라에게 return할 필요 없음
    @JsonIgnore
    private LocalDateTime createdAt;

    @QueryProjection
    public NanaThumbnail(Long id, String originUrl, String thumbnailUrl, String version,
        String subHeading,
        String heading, LocalDateTime createdAt) {
      this.id = id;
      this.firstImage = new ImageFileDto(originUrl, thumbnailUrl);
      this.version = version;
      this.subHeading = subHeading;
      this.heading = heading;
      this.createdAt = createdAt;
    }
  }

  @Data
  @Builder
  @Schema(description = "나나's pick 개별 상세 조회 DTO")
  public static class NanaDetailDto {

    @Schema(description = "게시물 ID")
    private Long id;
    
    @Schema(description = "사진에 들어갈 부제목 ex) 제주 야경 봤슴무언?")
    private String subHeading;

    @Schema(description = "사진에 들어갈 제목 ex) TOP 10 야경 맛집")
    private String heading;

    @Schema(description = "ex) nana's Pick vol.1")
    private String version;

    @Schema(description = "게시물 썸네일 이미지")
    private ImageFileDto firstImage;

    @NotBlank
    @Schema(description = "알아두면 좋아요! 내용")
    private String notice;

    @Schema(description = "좋아요 여부")
    private boolean isFavorite;

    @Schema(description = "게시물 데이터")
    private List<NanaDetail> nanaDetails;

  }

  @Data
  @Builder
  @Schema(description = "나나's pick 게시글 세부 내용")
  public static class NanaDetail {

    @Schema(description = "순위")
    public int number;

    @NotBlank
    @Schema(description = "부제목")
    public String subTitle;

    @NotBlank
    @Schema(description = "제목")
    public String title;

    @Schema(description = "게시물 이미지 리스트")
    private List<ImageFileDto> images;

    @NotBlank
    @Schema(description = "게시물 설명")
    public String content;

    public List<NanaAdditionalInfo> additionalInfoList;

    public List<String> hashtags;

  }

  @Data
  @Builder
  @Schema(description = "나나's pick 각 게시글 부가 정보")
  public static class NanaAdditionalInfo {

    @Schema(description = "이모지 구분을 위한 필드 /"
        + " ADDRESS, PARKING, SPECIAL, AMENITY, WEBSITE, RESERVATION_LINK, AGE, TIME, FEE, DATE, DESCRIPTION")
    public String infoEmoji;

    @Schema(description = "부가 정보 key 값 ex: 주차정보, 스페셜, 예약링크,,")
    public String infoKey;

    @Schema(description = "부가 정보 value 값")
    public String infoValue;

  }

  @Data
  @Builder
  @AllArgsConstructor
  @Schema(description = "나나's pick 개별 게시글 썸네일 조회 DTO")
  public static class NanaThumbnailPost {

    @Schema(description = "게시물 id")
    private Long id;

    @Schema(description = "게시물 썸네일 이미지")
    private ImageFileDto firstImage;

    @Schema(description = "제목 ex) TOP 10 야경 맛집")
    private String heading;


    @QueryProjection
    public NanaThumbnailPost(Long id, String originUrl, String thumbnailUrl, String heading) {
      this.id = id;
      this.firstImage = new ImageFileDto(originUrl, thumbnailUrl);
      this.heading = heading;
    }
  }
}
