package com.jeju.nanaland.domain.nature.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.dto.FavoriteResponse;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.Provider;
import com.jeju.nanaland.domain.nature.entity.Nature;
import com.jeju.nanaland.global.exception.BadRequestException;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class NatureServiceTest {

  @Autowired
  EntityManager em;
  @Autowired
  NatureService natureService;
  @Autowired
  FavoriteRepository favoriteRepository;

  Language language;
  Member member1, member2;
  Nature nature;
  Category category;

  @BeforeEach
  void init() {
    ImageFile imageFile1 = ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
    em.persist(imageFile1);

    ImageFile imageFile2 = ImageFile.builder()
        .originUrl("origin")
        .thumbnailUrl("thumbnail")
        .build();
    em.persist(imageFile2);

    language = Language.builder()
        .locale(Locale.KOREAN)
        .dateFormat("yy-mm-dd")
        .build();
    em.persist(language);

    member1 = Member.builder()
        .email("test@naver.com")
        .provider(Provider.KAKAO)
        .providerId(123456789L)
        .nickname("nickname1")
        .language(language)
        .profileImageFile(imageFile1)
        .build();
    em.persist(member1);

    member2 = Member.builder()
        .email("test2@naver.com")
        .provider(Provider.KAKAO)
        .providerId(1234567890L)
        .nickname("nickname2")
        .language(language)
        .profileImageFile(imageFile2)
        .build();
    em.persist(member2);

    nature = Nature.builder()
        .imageFile(imageFile1)
        .build();
    em.persist(nature);

    category = Category.builder()
        .content(CategoryContent.NATURE)
        .build();
    em.persist(category);
  }

  @Test
  void toggleLikeStatusTest() {
    /**
     * WHEN
     *
     * member1 : toggleLikeStatus 2번 적용
     * member2 : toggleLikeStatus 1번 적용
     */
    natureService.toggleLikeStatus(member1, nature.getId());
    FavoriteResponse.StatusDto result1 = natureService.toggleLikeStatus(member1,
        nature.getId());

    FavoriteResponse.StatusDto result2 = natureService.toggleLikeStatus(member2,
        nature.getId());

    /**
     * THEN
     *
     * member1 = 좋아요 X, member2 = 좋아요
     */
    assertThat(result1.isFavorite()).isFalse();
    assertThat(result2.isFavorite()).isTrue();
  }

  @Test
  void toggleLikeStatusFailedWithNoSuchPostIdTest() {
    /**
     * GIVEN
     *
     * 존재하지 않는 postId
     */
    Long postId = -1L;

    /**
     * WHEN
     * THEN
     *
     * toggleLikeStatus 요청 시 BadRequestException 발생
     */
    Assertions.assertThatThrownBy(() -> natureService.toggleLikeStatus(member1, postId))
        .isInstanceOf(BadRequestException.class);
  }
}