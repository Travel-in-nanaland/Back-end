package com.jeju.nanaland.domain.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.config.TestConfig;
import com.jeju.nanaland.domain.common.data.Category;
import com.jeju.nanaland.domain.common.data.Language;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.experience.entity.Experience;
import com.jeju.nanaland.domain.experience.entity.ExperienceTrans;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.enums.Provider;
import com.jeju.nanaland.domain.member.entity.enums.TravelType;
import com.jeju.nanaland.domain.review.dto.ReviewResponse.ReviewDetailDto;
import com.jeju.nanaland.domain.review.entity.Review;
import java.time.LocalDate;
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

  ImageFile imageFile;
  Member member;
  MemberInfoDto memberInfoDto;
  Experience experience;
  @Autowired
  TestEntityManager entityManager;
  @Autowired
  private ReviewRepository reviewRepository;

  @BeforeEach
  void setUp() {
    imageFile = createImageFile();
    memberInfoDto = createMemberInfoDto();
    experience = createExperience();
    createReviewList();
  }

  private ImageFile createImageFile() {
    imageFile = ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
    entityManager.persist(imageFile);
    return imageFile;
  }

  private MemberInfoDto createMemberInfoDto() {
    Language language = Language.KOREAN;
    member = Member.builder()
        .language(language)
        .email("test@example.com")
        .profileImageFile(imageFile)
        .nickname("testNickname")
        .gender("male")
        .birthDate(LocalDate.now())
        .provider(Provider.GOOGLE)
        .providerId("123")
        .travelType(TravelType.NONE)
        .build();
    entityManager.persist(member);

    return MemberInfoDto.builder()
        .member(member)
        .language(language)
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

  private void createReviewList() {
    for (int i = 0; i < 3; i++) {
      Review review = Review.builder()
          .member(member)
          .post(experience)
          .category(Category.EXPERIENCE)
          .content("content")
          .rating(5)
          .build();
      entityManager.persist(review);
    }
  }

  @Test
  @DisplayName("리뷰 리스트 조회")
  void findReviewListByPostId() {
    // given
    Category validCategory = Category.EXPERIENCE;
    Pageable pageable = PageRequest.of(0, 2);

    // when
    Page<ReviewDetailDto> reviewDetailDtos = reviewRepository.findReviewListByPostId(
        memberInfoDto, validCategory, experience.getId(), pageable);

    // then
    assertThat(reviewDetailDtos).isNotNull();
    assertThat(reviewDetailDtos.getTotalElements()).isEqualTo(3);
    assertThat(reviewDetailDtos.getContent()).isNotEmpty();
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
}