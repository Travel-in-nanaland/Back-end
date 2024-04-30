package com.jeju.nanaland.domain.festival.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.festival.dto.FestivalResponse.FestivalThumbnailDto;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.festival.entity.FestivalTrans;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.Provider;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FestivalServiceTest {

  @Autowired
  EntityManager em;
  @Autowired
  FestivalService festivalService;
  @Autowired
  FavoriteRepository favoriteRepository;

  Language language;
  Member member1, member2;
  MemberInfoDto memberInfoDto1, memberInfoDto2;
  Festival festival;
  FestivalTrans festivalTrans;
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

    memberInfoDto1 = MemberInfoDto.builder()
        .language(language)
        .member(member1)
        .build();

    memberInfoDto2 = MemberInfoDto.builder()
        .language(language)
        .member(member2)
        .build();

    festival = Festival.builder()
        .imageFile(imageFile1)
        .startDate(LocalDate.parse("2023-08-07"))
        .endDate(LocalDate.parse("2023-08-10"))
        .season("봄, 여름")
        .build();
    em.persist(festival);

    festivalTrans = FestivalTrans.builder()
        .festival(festival)
        .fee("fee1")
        .time("time1")
        .title("festivalTitle1")
        .intro("festivalIntro1")
        .address("제주시 우도")
        .addressTag("우도")
        .content("content1")
        .language(language)
        .build();
    em.persist(festivalTrans);

    category = Category.builder()
        .content(CategoryContent.FESTIVAL)
        .build();
    em.persist(category);
  }

  @Test
  void getSeasonFestivalListTest() {
    FestivalThumbnailDto summerFestival1 = festivalService.getSeasonFestivalList(Locale.KOREAN, 0,
        1,
        "summer");
    FestivalThumbnailDto summerFestival2 = festivalService.getSeasonFestivalList(Locale.KOREAN, 0,
        1,
        "autumn");

    Assertions.assertThat(summerFestival1.getTotalElements()).isSameAs(1L);
    Assertions.assertThat(summerFestival2.getTotalElements()).isSameAs(0L);
  }
}