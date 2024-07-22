package com.jeju.nanaland.domain.review.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.REVIEW_CREATED_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.REVIEW_HEART_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.REVIEW_LIST_SUCCESS;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.review.dto.ReviewRequest;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewListDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewPreviewDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewListDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.StatusDto;
import com.jeju.nanaland.domain.review.service.ReviewService;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.auth.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/review")
@Tag(name = "리뷰(Review)", description = "리뷰(Review) API입니다.")
public class ReviewController {

  private final ReviewService reviewService;

  @Operation(summary = "게시물 별 리뷰 리스트 조회", description = "리뷰 리스트 조회 (페이징)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 데이터인 경우", content = @Content)
  })
  @GetMapping("/list/{id}")
  public BaseResponse<ReviewListDto> getReviewList(
      @AuthMember MemberInfoDto memberInfoDto,
      @PathVariable Long id,
      @RequestParam Category category,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size
  ) {
    ReviewListDto reviewList = reviewService.getReviewList(memberInfoDto, category, id, page, size);
    return BaseResponse.success(REVIEW_LIST_SUCCESS, reviewList);
  }

  @Operation(summary = "리뷰 생성", description = "게시물에 대한 리뷰 작성")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
//      @ApiResponse(responseCode = "404", description = "존재하지 않는 데이터인 경우", content = @Content)
  })
  @PostMapping(value = "{id}",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public BaseResponse<String> uploadReview(
      @AuthMember MemberInfoDto memberInfoDto,
      @PathVariable Long id,
      @RequestParam Category category,
      @RequestPart(required = false) List<MultipartFile> imageList,
      @RequestPart @Valid ReviewRequest.CreateReviewDto createReviewDto
  ) {
    reviewService.saveReview(memberInfoDto, id, category, createReviewDto, imageList);
    return BaseResponse.success(REVIEW_CREATED_SUCCESS);
  }

  @Operation(summary = "리뷰 좋아요 토글")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 데이터인 경우", content = @Content)
  })
  @PostMapping("/heart/{id}")
  public BaseResponse<StatusDto> toggleReviewHeart(
      @AuthMember MemberInfoDto memberInfoDto,
      @PathVariable Long id) {
    StatusDto statusDto = reviewService.toggleReviewHeart(memberInfoDto, id);
    return BaseResponse.success(REVIEW_HEART_SUCCESS, statusDto);
  }

  @Operation(summary = "회원 별 리뷰 썸네일 리스트 조회(6개 ~ 12개)", description = "회원 별 리뷰 썸네일 리스트 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 데이터인 경우", content = @Content)
  })
  @GetMapping("/preview")
  public BaseResponse<MemberReviewPreviewDto> getReviewList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(required = false) Long memberId
  ) {
    MemberReviewPreviewDto reviewList = reviewService.getReviewPreviewByMember(memberInfoDto,
        memberId);
    return BaseResponse.success(REVIEW_LIST_SUCCESS, reviewList);
  }

  @Operation(summary = "회원 별 리뷰 리스트 조회", description = "회원 별 리뷰 리스트 조회 (페이징)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "성공"),
      @ApiResponse(responseCode = "401", description = "accessToken이 유효하지 않은 경우", content = @Content),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 데이터인 경우", content = @Content)
  })
  @GetMapping("/list")
  public BaseResponse<MemberReviewListDto> getReviewList(
      @AuthMember MemberInfoDto memberInfoDto,
      @RequestParam(required = false) Long memberId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size
  ) {
    MemberReviewListDto reviewList = reviewService.getReviewListByMember(memberInfoDto, memberId,
        page, size);
    return BaseResponse.success(REVIEW_LIST_SUCCESS, reviewList);
  }
}
