package com.jeju.nanaland.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.dto.ImageFileDto;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.member.repository.MemberRepository;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewListDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewPreviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewPreviewDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewListDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewStatusDto;
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

  ImageFile imageFile;
  Member member;
  MemberInfoDto memberInfoDto;
  Review review;
  ReviewHeart reviewHeart;
  @InjectMocks
  private ReviewService reviewService;
  @Mock
  private ReviewRepository reviewRepository;
  @Mock
  private ReviewHeartRepository reviewHeartRepository;
  @Mock
  private MemberRepository memberRepository;

  @BeforeEach
  void setUp() {
    imageFile = createImageFile();
    Language language = Language.KOREAN;
    member = createMember(language);
    memberInfoDto = createMemberInfoDto(language, member);
    review = createReview();
    reviewHeart = createReviewHeart(memberInfoDto.getMember(), review);
  }

  private ImageFile createImageFile() {
    return ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
  }

  private Member createMember(Language language) {
    return spy(Member.builder()
        .language(language)
        .email("test@example.com")
        .profileImageFile(imageFile)
        .nickname("testNickname")
        .gender("male")
        .birthDate(LocalDate.now())
        .provider(Provider.GOOGLE)
        .providerId("123")
        .travelType(TravelType.GAMGYUL)
        .build());
  }

  private MemberInfoDto createMemberInfoDto(Language language, Member member) {
    return MemberInfoDto.builder()
        .language(language)
        .member(member)
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
              .rating(4.5)
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
        .member(member)
        .build();
  }

  private ReviewHeart createReviewHeart(Member member, Review review) {
    return ReviewHeart.builder()
        .member(member)
        .review(review)
        .build();
  }

  private Page<MemberReviewDetailDto> createMemberReviewDetailList() {
    List<MemberReviewDetailDto> reviewDetailDtos = new ArrayList<>();
    for (int i = 1; i < 3; i++) {
      reviewDetailDtos.add(
          MemberReviewDetailDto.builder()
              .id((long) i)
              .postId(1L)
              .category(Category.EXPERIENCE)
              .placeName("title")
              .createdAt(LocalDate.now())
              .heartCount(5)
              .build());
    }

    return new PageImpl<>(reviewDetailDtos, PageRequest.of(0, 2), 10);
  }

  private List<MemberReviewPreviewDetailDto> createMemberReviewPreviewDetailList() {
    List<MemberReviewPreviewDetailDto> reviewDetailDtos = new ArrayList<>();
    for (int i = 1; i < 15; i++) {
      reviewDetailDtos.add(
          MemberReviewPreviewDetailDto.builder()
              .id((long) i)
              .postId(1L)
              .category(Category.EXPERIENCE)
              .placeName("title")
              .createdAt(LocalDate.now())
              .heartCount(5)
              .build());
    }

    return reviewDetailDtos;
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
  @DisplayName("리뷰 좋아요 토글 성공 - 본인이 작성한 리뷰인 경우")
  void toggleReviewHeartFail2() {
    // given
    Long reviewId = 1L;

    doReturn(Optional.of(review)).when(reviewRepository).findById(reviewId);

    // when
    BadRequestException badRequestException = assertThrows(BadRequestException.class,
        () -> reviewService.toggleReviewHeart(memberInfoDto, reviewId));

    // then
    assertThat(badRequestException.getMessage()).isEqualTo(
        ErrorCode.REVIEW_SELF_LIKE_FORBIDDEN.getMessage());
  }

  @Test
  @DisplayName("리뷰 좋아요 토글 성공 - 좋아요가 이미 존재하는 경우")
  void toggleReviewHeartSuccess() {
    // given
    Long reviewId = 1L;
    Member member2 = createMember(Language.ENGLISH);
    MemberInfoDto memberInfoDto2 = createMemberInfoDto(Language.ENGLISH, member2);

    doReturn(Optional.of(review)).when(reviewRepository).findById(reviewId);
    doReturn(Optional.of(reviewHeart)).when(reviewHeartRepository)
        .findByMemberAndReview(memberInfoDto2.getMember(), review);

    // when
    ReviewStatusDto statusDto = reviewService.toggleReviewHeart(memberInfoDto2, reviewId);

    // then
    assertThat(statusDto.isReviewHeart()).isFalse();
    verify(reviewHeartRepository, times(1)).delete(reviewHeart);
  }

  @Test
  @DisplayName("리뷰 좋아요 토글 성공 - 좋아요가 존재하지 않는 경우")
  void toggleReviewHeartSuccess2() {
    // given
    Long reviewId = 1L;
    Member member2 = createMember(Language.ENGLISH);
    MemberInfoDto memberInfoDto2 = createMemberInfoDto(Language.ENGLISH, member2);

    doReturn(Optional.of(review)).when(reviewRepository).findById(reviewId);
    doReturn(Optional.empty()).when(reviewHeartRepository)
        .findByMemberAndReview(memberInfoDto2.getMember(), review);

    // when
    ReviewStatusDto statusDto = reviewService.toggleReviewHeart(memberInfoDto2, reviewId);

    // then
    assertThat(statusDto.isReviewHeart()).isTrue();
    verify(reviewHeartRepository, times(1)).save(any(ReviewHeart.class));
  }

  @Test
  @DisplayName("회원 리뷰 리스트 조회 실패 - 존재하지 않는 회원인 경우")
  void getReviewListByMemberFail() {
    // given
    doReturn(1L).when(member).getId();

    // when
    NotFoundException notFoundException = assertThrows(NotFoundException.class,
        () -> reviewService.getReviewListByMember(memberInfoDto, 2L, 0, 12));

    // then
    assertThat(notFoundException.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("내 리뷰 리스트 조회 성공")
  void getReviewListByMemberSuccess() {
    // given
    doReturn(1L).when(member).getId();
    Page<MemberReviewDetailDto> memberReviewDetailList = createMemberReviewDetailList();
    doReturn(memberReviewDetailList).when(reviewRepository)
        .findReviewListByMember(any(), any(), any());

    // when
    MemberReviewListDto reviewListByMember = reviewService.getReviewListByMember(memberInfoDto,
        null, 0, 2);
    MemberReviewListDto reviewListByMember2 = reviewService.getReviewListByMember(memberInfoDto,
        1L, 0, 2);

    // then
    assertThat(reviewListByMember).isNotNull();
    assertThat(reviewListByMember.getTotalElements()).isEqualTo(
        memberReviewDetailList.getTotalElements());
    assertThat(reviewListByMember.getData()).hasSameSizeAs(memberReviewDetailList.getContent());
    assertThat(reviewListByMember2).isNotNull();
    assertThat(reviewListByMember2.getTotalElements()).isEqualTo(
        memberReviewDetailList.getTotalElements());
    assertThat(reviewListByMember2.getData()).hasSameSizeAs(memberReviewDetailList.getContent());
  }

  @Test
  @DisplayName("타인 리뷰 리스트 조회 성공")
  void getReviewListByMemberSuccess2() {
    // given
    Page<MemberReviewDetailDto> memberReviewDetailList = createMemberReviewDetailList();
    Language language = Language.ENGLISH;
    Member member2 = createMember(language);

    doReturn(1L).when(member).getId();
    doReturn(Optional.of(member2)).when(memberRepository).findById(any());
    doReturn(memberReviewDetailList).when(reviewRepository)
        .findReviewListByMember(any(), any(), any());

    // when
    MemberReviewListDto reviewListByMember = reviewService.getReviewListByMember(memberInfoDto,
        2L, 0, 2);

    // then
    assertThat(reviewListByMember).isNotNull();
    assertThat(reviewListByMember.getTotalElements()).isEqualTo(
        memberReviewDetailList.getTotalElements());
    assertThat(reviewListByMember.getData()).hasSameSizeAs(memberReviewDetailList.getContent());
  }

  @Test
  @DisplayName("회원 리뷰 미리보기 리스트 조회 실패 - 존재하지 않는 회원인 경우")
  void getReviewPreviewByMemberFail() {
    // given
    doReturn(1L).when(member).getId();

    // when
    NotFoundException notFoundException = assertThrows(NotFoundException.class,
        () -> reviewService.getReviewPreviewByMember(memberInfoDto, 2L));

    // then
    assertThat(notFoundException.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("내 리뷰 미리보기 리스트 조회 성공")
  void getReviewPreviewByMemberSuccess() {
    // given
    doReturn(1L).when(member).getId();
    List<MemberReviewPreviewDetailDto> previewDetailList = createMemberReviewPreviewDetailList();
    doReturn(previewDetailList.subList(0, 12)).when(reviewRepository)
        .findReviewPreviewByMember(any(), any());
    doReturn((long) previewDetailList.size()).when(reviewRepository)
        .findTotalCountByMember(any());

    // when
    MemberReviewPreviewDto reviewListByMember = reviewService.getReviewPreviewByMember(
        memberInfoDto, null);
    MemberReviewPreviewDto reviewListByMember2 = reviewService.getReviewPreviewByMember(
        memberInfoDto, 1L);

    // then
    assertThat(reviewListByMember).isNotNull();
    assertThat(reviewListByMember.getTotalElements()).isEqualTo(
        previewDetailList.size());
    assertThat(reviewListByMember.getData()).hasSize(12);
    assertThat(reviewListByMember2).isNotNull();
    assertThat(reviewListByMember2.getTotalElements()).isEqualTo(
        previewDetailList.size());
    assertThat(reviewListByMember2.getData()).hasSize(12);
  }

  @Test
  @DisplayName("타인 리뷰 미리보기 리스트 조회 성공")
  void getReviewPreviewByMemberSuccess2() {
    // given
    Language language = Language.ENGLISH;
    Member member2 = createMember(language);
    List<MemberReviewPreviewDetailDto> previewDetailList = createMemberReviewPreviewDetailList();

    doReturn(1L).when(member).getId();
    doReturn(Optional.of(member2)).when(memberRepository).findById(any());
    doReturn(previewDetailList.subList(0, 12)).when(reviewRepository)
        .findReviewPreviewByMember(any(), any());
    doReturn((long) previewDetailList.size()).when(reviewRepository)
        .findTotalCountByMember(any());

    // when
    MemberReviewPreviewDto reviewListByMember = reviewService.getReviewPreviewByMember(
        memberInfoDto, 2L);

    assertThat(reviewListByMember).isNotNull();
    assertThat(reviewListByMember.getTotalElements()).isEqualTo(
        previewDetailList.size());
    assertThat(reviewListByMember.getData()).hasSize(12);
  }
}