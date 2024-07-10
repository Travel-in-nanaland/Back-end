package com.jeju.nanaland.domain.review.controller;

import static com.jeju.nanaland.global.exception.SuccessCode.REVIEW_CREATED_SUCCESS;
import static com.jeju.nanaland.global.exception.SuccessCode.REVIEW_LIST_SUCCESS;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.review.dto.ReviewRequest;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewListDto;
import com.jeju.nanaland.domain.review.service.ReviewService;
import com.jeju.nanaland.global.BaseResponse;
import com.jeju.nanaland.global.auth.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

  @Operation(summary = "리뷰 리스트 조회", description = "리뷰 리스트 조회 (페이징)")
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

  @Operation(summary = "리뷰 리스트 생성", description = "유저가 리스트 작성")
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
      @RequestPart List<MultipartFile> imageList,
      @RequestPart ReviewRequest.CreateReviewDto createReviewDto
  ) {
    reviewService.saveReview(memberInfoDto, id, category, createReviewDto, imageList);
    return BaseResponse.success(REVIEW_CREATED_SUCCESS);
  }


}
