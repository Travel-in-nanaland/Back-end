package com.jeju.nanaland.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewListDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.StatusDto;
import com.jeju.nanaland.domain.review.entity.Review;
import com.jeju.nanaland.domain.review.entity.ReviewHeart;
import com.jeju.nanaland.domain.review.repository.ReviewHeartRepository;
import com.jeju.nanaland.domain.review.repository.ReviewRepository;
import com.jeju.nanaland.global.exception.BadRequestException;
import com.jeju.nanaland.global.exception.ErrorCode;
import com.jeju.nanaland.global.exception.NotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
@Execution(ExecutionMode.CONCURRENT)
class ReviewServiceTest {

  MemberInfoDto memberInfoDto;
  Review review;
  ReviewHeart reviewHeart;
  @InjectMocks
  private ReviewService reviewService;
  @Mock
  private ReviewRepository reviewRepository;
  @Mock
  private ReviewHeartRepository reviewHeartRepository;

  @BeforeEach
  void setUp() {
    memberInfoDto = createMemberInfoDto();
    review = createReview();
    reviewHeart = createReviewHeart(memberInfoDto.getMember(), review);
  }

  private MemberInfoDto createMemberInfoDto() {
    Language language = Language.KOREAN;
    Member member = Member.builder()
        .language(language)
        .travelType(TravelType.NONE)
        .build();

    return MemberInfoDto.builder()
        .member(member)
        .language(language)
        .build();
  }

  private Page<ReviewDetailDto> createReviewDetailList() {
    List<ReviewDetailDto> reviewDetailDtos = new ArrayList<>();
    for (int i = 1; i < 3; i++) {
      reviewDetailDtos.add(
          ReviewDetailDto.builder()
              .id((long) i)
              .memberId(1L)
              .nickname("nickname")
              .profileImage(new ImageFileDto("originUrl", "thumbnailUrl"))
              .memberReviewCount(10)
              .memberReviewAvgRating(4.5)
              .content("content")
              .createdAt(LocalDate.now())
              .heartCount(5)
              .isReviewHeart(false)
              .images(Collections.emptyList())
              .reviewTypeKeywords(Set.of("keyword1", "keyword2"))
              .build());
    }

    return new PageImpl<>(reviewDetailDtos, PageRequest.of(0, 2), 10);
  }

  private Review createReview() {
    return Review.builder()
        .category(Category.EXPERIENCE)
        .content("content")
        .build();
  }

  private ReviewHeart createReviewHeart(Member member, Review review) {
    return ReviewHeart.builder()
        .member(member)
        .review(review)
        .build();
  }

  @Test
  @DisplayName("리뷰 리스트 조회 실패 - 리뷰가 존재하지 않는 카테고리인 경우")
  void getReviewListFail() {
    // given
    Category invalidCategory = Category.MARKET;

    // when
    BadRequestException badRequestException = assertThrows(BadRequestException.class,
        () -> reviewService.getReviewList(memberInfoDto, invalidCategory, 1L, 0, 2));

    // then
    assertThat(badRequestException.getMessage()).isEqualTo(
        ErrorCode.REVIEW_INVALID_CATEGORY.getMessage());
  }

  @Test
  @DisplayName("리뷰 리스트 조회 성공")
  void getReviewListSuccess() {
    // given
    Category validCategory = Category.EXPERIENCE;
    Page<ReviewDetailDto> reviewPage = createReviewDetailList();
    Double totalAvgRating = 5.0;

    doReturn(reviewPage).when(reviewRepository)
        .findReviewListByPostId(any(), any(Category.class), any(), any());
    doReturn(totalAvgRating).when(reviewRepository)
        .findTotalRatingAvg(any(), any());

    // when
    ReviewListDto reviewListDto = reviewService.getReviewList(memberInfoDto, validCategory, 1L, 0,
        2);

    // then
    assertThat(reviewListDto).isNotNull();
    assertThat(reviewListDto.getTotalElements()).isEqualTo(reviewPage.getTotalElements());
    assertThat(reviewListDto.getTotalAvgRating()).isEqualTo(totalAvgRating);
    assertThat(reviewListDto.getData()).hasSameSizeAs(reviewPage.getContent());
  }

  @Test
  @DisplayName("리뷰 좋아요 토글 실패 - 리뷰가 존재하지 않는 경우")
  void toggleReviewHeartFail() {
    // given
    Long reviewId = 1L;

    doReturn(Optional.empty()).when(reviewRepository).findById(reviewId);

    // when
    NotFoundException notFoundException = assertThrows(NotFoundException.class,
        () -> reviewService.toggleReviewHeart(memberInfoDto, reviewId));

    // then
    assertThat(notFoundException.getMessage()).isEqualTo(ErrorCode.REVIEW_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("리뷰 좋아요 토글 성공 - 좋아요가 이미 존재하는 경우")
  void toggleReviewHeartSuccess() {
    // given
    Long reviewId = 1L;

    doReturn(Optional.of(review)).when(reviewRepository).findById(reviewId);
    doReturn(Optional.of(reviewHeart)).when(reviewHeartRepository)
        .findByMemberAndReview(memberInfoDto.getMember(), review);

    // when
    StatusDto statusDto = reviewService.toggleReviewHeart(memberInfoDto, reviewId);

    // then
    assertThat(statusDto.isReviewHeart()).isFalse();
    verify(reviewHeartRepository, times(1)).delete(reviewHeart);
  }

  @Test
  @DisplayName("리뷰 좋아요 토글 성공 - 좋아요가 존재하지 않는 경우")
  void toggleReviewHeartSuccess2() {
    // given
    Long reviewId = 1L;

    doReturn(Optional.of(review)).when(reviewRepository).findById(reviewId);
    doReturn(Optional.empty()).when(reviewHeartRepository)
        .findByMemberAndReview(memberInfoDto.getMember(), review);

    // when
    StatusDto statusDto = reviewService.toggleReviewHeart(memberInfoDto, reviewId);

    // then
    assertThat(statusDto.isReviewHeart()).isTrue();
    verify(reviewHeartRepository, times(1)).save(any(ReviewHeart.class));
  }
}