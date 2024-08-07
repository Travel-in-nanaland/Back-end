package com.jeju.nanaland.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Post;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.experience.entity.ExperienceTrans;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.report.entity.review.ClaimType;
import com.jeju.nanaland.domain.report.entity.review.ReviewReport;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.MemberReviewPreviewDetailDto;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewDetailDto;
import com.jeju.nanaland.domain.review.entity.Review;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import(TestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewRepositoryImplTest {

  ImageFile imageFile, imageFile2;
  Member member1, member2;
  Experience experience;
  List<Review> reviewList, reviewList2;
  @Autowired
  TestEntityManager entityManager;
  @Autowired
  private ReviewRepository reviewRepository;

  @BeforeEach
  void setUp() {
    imageFile = createImageFile();
    member1 = createMember("nickname", imageFile);
    experience = createExperience();
    reviewList = createReviewList(experience, member1, 3);
    imageFile2 = createImageFile();
    member2 = createMember("nickname2", imageFile2);
    reviewList2 = createReviewList(experience, member2, 2);
  }

  private ImageFile createImageFile() {
    imageFile = ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
    entityManager.persist(imageFile);
    return imageFile;
  }

  private Member createMember(String nickname, ImageFile imageFile) {
    Language language = Language.KOREAN;
    Member member = Member.builder()
        .language(language)
        .email("test@example.com")
        .profileImageFile(imageFile)
        .nickname(nickname)
        .gender("male")
        .birthDate(LocalDate.now())
        .provider(Provider.GOOGLE)
        .providerId("123")
        .travelType(TravelType.NONE)
        .build();
    entityManager.persist(member);
    return member;
  }

  private MemberInfoDto createMemberInfoDto(Member member) {
    return MemberInfoDto.builder()
        .member(member)
        .language(member.getLanguage())
        .build();
  }

  private Experience createExperience() {
    Experience experience = Experience.builder()
        .firstImageFile(imageFile)
        .priority((long) 1)
        .build();
    entityManager.persistAndFlush(experience);
    ExperienceTrans experienceTrans = ExperienceTrans.builder()
        .experience(experience)
        .title("experience title")
        .language(Language.KOREAN)
        .addressTag("제주시")
        .build();
    entityManager.persist(experienceTrans);
    return experience;
  }

  private List<Review> createReviewList(Post post, Member member, int count) {
    List<Review> reviews = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      Review review = Review.builder()
          .member(member)
          .post(post)
          .category(Category.EXPERIENCE)
          .content("content")
          .rating(5)
          .build();
      entityManager.persist(review);
      reviews.add(review);
    }
    return reviews;
  }

  private void createReviewReport(Long id, Member member) {
    ReviewReport reviewReport = ReviewReport.builder()
        .reviewId(id)
        .claimType(ClaimType.DISLIKE)
        .member(member)
        .content("content")
        .build();
    entityManager.persist(reviewReport);
  }

  @Test
  @DisplayName("게시물 별 리뷰 리스트 조회")
  void findReviewListByPostId() {
    // given
    Category validCategory = Category.EXPERIENCE;
    Pageable pageable = PageRequest.of(0, 2);
    MemberInfoDto memberInfoDto = createMemberInfoDto(member1);

    // when
    Page<ReviewDetailDto> reviewDetails = reviewRepository.findReviewListByPostId(
        memberInfoDto, validCategory, experience.getId(), pageable);

    // then
    assertThat(reviewDetails).isNotNull();
    assertThat(reviewDetails.getTotalElements()).isEqualTo(5);
    assertThat(reviewDetails.getContent()).isNotEmpty();
  }

  @Test
  @DisplayName("리뷰 리스트 조회 - 신고한 리뷰는 안보이도록")
  void findReviewListByPostId2() {
    // given - member2가 member1의 첫 번째 리뷰 신고
    MemberInfoDto member2InfoDto = createMemberInfoDto(member2);
    createReviewReport(reviewList.get(0).getId(), member2);

    Category validCategory = Category.EXPERIENCE;
    Pageable pageable = PageRequest.of(0, 2);

    // when
    Page<ReviewDetailDto> reviewDetails = reviewRepository.findReviewListByPostId(
        member2InfoDto, validCategory, experience.getId(), pageable);

    // then
    assertThat(reviewDetails).isNotNull();
    assertThat(reviewDetails.getTotalElements()).isEqualTo(4);
    assertThat(reviewDetails.getContent()).isNotEmpty();
  }

  @Test
  @DisplayName("리뷰 전체 평균 점수")
  void findTotalRatingAvg() {
    // given
    Category validCategory = Category.EXPERIENCE;
    Double totalAvgRating = 5.0;

    // when
    Double avgRating = reviewRepository.findTotalRatingAvg(validCategory, experience.getId());

    // then
    assertThat(avgRating).isEqualTo(totalAvgRating);
  }

  @Test
  @DisplayName("회원 별 리뷰 리스트 조회 - 내 프로필 조회")
  void findReviewListByMember() {
    // given
    Pageable pageable = PageRequest.of(0, 2);

    // when
    Page<MemberReviewDetailDto> memberReviewDetails = reviewRepository.findReviewListByMember(
        member1.getId(), member1, member1.getLanguage(), pageable);

    // then
    assertThat(memberReviewDetails.getTotalElements()).isEqualTo(3);
    MemberReviewDetailDto dto = memberReviewDetails.getContent().get(0);
    assertThat(dto.getId()).isNotNull();
    assertThat(dto.getPostId()).isEqualTo(experience.getId());
    assertThat(dto.getCategory()).isEqualTo(Category.EXPERIENCE);
    assertThat(dto.getPlaceName()).isEqualTo("experience title");
  }

  @Test
  @DisplayName("회원 별 리뷰 리스트 조회 - 타인 프로필 조회")
  void findReviewListByMember2() {
    // given
    Pageable pageable = PageRequest.of(0, 2);

    // when - member1이 member2 조회
    Page<MemberReviewDetailDto> memberReviewDetails = reviewRepository.findReviewListByMember(
        member1.getId(), member2, member1.getLanguage(), pageable);

    // then
    assertThat(memberReviewDetails.getTotalElements()).isEqualTo(2);
    MemberReviewDetailDto dto = memberReviewDetails.getContent().get(0);
    assertThat(dto.getId()).isNotNull();
    assertThat(dto.getPostId()).isEqualTo(experience.getId());
    assertThat(dto.getCategory()).isEqualTo(Category.EXPERIENCE);
    assertThat(dto.getPlaceName()).isEqualTo("experience title");
  }

  @Test
  @DisplayName("회원 별 리뷰 리스트 조회 - 타인 프로필 조회 - 신고한 리뷰는 안보이도록")
  void findReviewListByMember3() {
    // given - member1이 member2 첫 번째 리뷰 신고
    Pageable pageable = PageRequest.of(0, 2);
    createReviewReport(reviewList2.get(0).getId(), member1);

    // when
    Page<MemberReviewDetailDto> memberReviewDetails = reviewRepository.findReviewListByMember(
        member1.getId(), member2, member1.getLanguage(), pageable);

    // then
    assertThat(memberReviewDetails.getTotalElements()).isEqualTo(1);
    MemberReviewDetailDto dto = memberReviewDetails.getContent().get(0);
    assertThat(dto.getId()).isNotNull();
    assertThat(dto.getPostId()).isEqualTo(experience.getId());
    assertThat(dto.getCategory()).isEqualTo(Category.EXPERIENCE);
    assertThat(dto.getPlaceName()).isEqualTo("experience title");
  }

  @Test
  @DisplayName("회원 별 리뷰 썸네일 리스트 조회 - 내 프로필")
  void findReviewPreviewByMember() {
    // when
    List<MemberReviewPreviewDetailDto> memberReviewPreviewDetails = reviewRepository.findReviewPreviewByMember(
        member1.getId(), member1, member1.getLanguage());

    // then
    assertThat(memberReviewPreviewDetails).hasSize(3);
    MemberReviewPreviewDetailDto dto = memberReviewPreviewDetails.get(0);
    assertThat(dto.getId()).isNotNull();
    assertThat(dto.getPostId()).isEqualTo(experience.getId());
    assertThat(dto.getCategory()).isEqualTo(Category.EXPERIENCE);
    assertThat(dto.getPlaceName()).isEqualTo("experience title");
  }

  @Test
  @DisplayName("회원 별 리뷰 썸네일 리스트 조회 - 타인 프로필")
  void findReviewPreviewByMember2() {
    // when - member1이 member2 조회
    List<MemberReviewPreviewDetailDto> memberReviewPreviewDetails = reviewRepository.findReviewPreviewByMember(
        member1.getId(), member2, member1.getLanguage());

    // then
    assertThat(memberReviewPreviewDetails).hasSize(2);
    MemberReviewPreviewDetailDto dto = memberReviewPreviewDetails.get(0);
    assertThat(dto.getId()).isNotNull();
    assertThat(dto.getPostId()).isEqualTo(experience.getId());
    assertThat(dto.getCategory()).isEqualTo(Category.EXPERIENCE);
    assertThat(dto.getPlaceName()).isEqualTo("experience title");
  }

  @Test
  @DisplayName("회원 별 리뷰 썸네일 리스트 조회 - 타인 프로필 - 신고한 리뷰는 안보이도록")
  void findReviewPreviewByMember3() {
    // given
    createReviewReport(reviewList2.get(0).getId(), member1);

    // when - member1이 member2 조회
    List<MemberReviewPreviewDetailDto> memberReviewPreviewDetails = reviewRepository.findReviewPreviewByMember(
        member1.getId(), member2, member1.getLanguage());

    // then
    assertThat(memberReviewPreviewDetails).hasSize(1);
    MemberReviewPreviewDetailDto dto = memberReviewPreviewDetails.get(0);
    assertThat(dto.getId()).isNotNull();
    assertThat(dto.getPostId()).isEqualTo(experience.getId());
    assertThat(dto.getCategory()).isEqualTo(Category.EXPERIENCE);
    assertThat(dto.getPlaceName()).isEqualTo("experience title");
  }

  @Test
  @DisplayName("회원이 작성한 리뷰 총 개수")
  void findTotalCountByMember() {
    // when
    Long totalCountByMember = reviewRepository.findTotalCountByMember(member1);

    // then
    assertThat(totalCountByMember).isEqualTo(3);
  }

  @Test
  void findExperienceMyReviewDetail() {
  }

  @Test
  void findRestaurantMyReviewDetail() {
  }
}