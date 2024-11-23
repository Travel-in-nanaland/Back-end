package com.jeju.nanaland.domain.festival.dto;

import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

public class FestivalResponse {

  @Data
  @Builder
  @Schema(description = "축제 썸네일 조회 DTO")
  public static class FestivalThumbnailDto {

    @Schema(description = "총 조회 개수")
    private Long totalElements;

    @Schema(description = "결과 데이터")
    private List<FestivalThumbnail> data;


  }

  @Data
  @Builder
  @Schema(description = "축제 썸네일 조회 DTO")
  public static class FestivalThumbnail {

    @Schema(description = "축제 게시물 id")
    private Long id;

    @Schema(description = "축제 이름")
    private String title;

    @NotBlank
    @Schema(description = "축제 썸네일 이미지")
    private ImageFileDto firstImage;

    @Schema(description = "주소 태그")
    private String addressTag;

    @NotBlank
    private String period;

    @Schema(description = "좋아요 여부")
    private boolean isFavorite;

  }

  @Builder
  @Getter
  @Schema(description = "축제 상세 정보")
  public static class FestivalDetailDto {

    @Schema(description = "7대 자연 게시물 id")
    private Long id;

    @Schema(description = "축제 진행 여부")
    private boolean onGoing;

    @Schema(description = "이미지 리스트")
    private List<ImageFileDto> images;

    @Schema(description = "주소 태그")
    private String addressTag;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "본문")
    private String content;

    @Schema(description = "주소")
    private String address;

    @Schema(description = "연락처")
    private String contact;

    @Schema(description = "이용 시간")
    private String time;

    @Schema(description = "입장료")
    private String fee;

    @Schema(description = "홈페이지")
    private String homepage;

    @Schema(description = "기간")
    private String period;

    @Schema(description = "좋아요 여부")
    private boolean isFavorite;

  }
}
