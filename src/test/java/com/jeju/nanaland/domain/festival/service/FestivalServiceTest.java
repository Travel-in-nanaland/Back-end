package com.jeju.nanaland.domain.festival.service;

import com.jeju.nanaland.domain.common.data.CategoryContent;
import com.jeju.nanaland.domain.common.entity.Category;
import com.jeju.nanaland.domain.common.entity.ImageFile;
import com.jeju.nanaland.domain.common.entity.Language;
import com.jeju.nanaland.domain.common.entity.Locale;
import com.jeju.nanaland.domain.favorite.dto.FavoriteRequest;
import com.jeju.nanaland.domain.favorite.dto.FavoriteRequest.LikeToggleDto;
import com.jeju.nanaland.domain.favorite.repository.FavoriteRepository;
import com.jeju.nanaland.domain.favorite.service.FavoriteService;
import com.jeju.nanaland.domain.festival.dto.FestivalResponse.FestivalThumbnailDto;
import com.jeju.nanaland.domain.festival.entity.Festival;
import com.jeju.nanaland.domain.festival.entity.FestivalTrans;
import com.jeju.nanaland.domain.member.dto.MemberResponse.MemberInfoDto;
import com.jeju.nanaland.domain.member.entity.Member;
import com.jeju.nanaland.domain.member.entity.Provider;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Collections;
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
  @Autowired
  FavoriteService favoriteService;

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
    FavoriteRequest.LikeToggleDto festivalLikeToggleDto = new LikeToggleDto();
    //좋아요 누르기
    festivalLikeToggleDto.setId(1L);
    festivalLikeToggleDto.setCategory("FESTIVAL");
    favoriteService.toggleLikeStatus(memberInfoDto1, festivalLikeToggleDto);

    FestivalThumbnailDto summerFestival1 = festivalService.getSeasonFestivalList(memberInfoDto1, 0,
        1,
        "summer");
    FestivalThumbnailDto summerFestival2 = festivalService.getSeasonFestivalList(memberInfoDto1, 0,
        1,
        "autumn");

    // 좋아요 반환 되는지
    Assertions.assertThat(summerFestival1.getData().get(0).isFavorite()).isTrue();

    // 여름 축제 반환 (데이터 있음)
    Assertions.assertThat(summerFestival1.getTotalElements()).isSameAs(1L);

    // 여름 축제 반환 (데이터 없음)
    Assertions.assertThat(summerFestival2.getTotalElements()).isSameAs(0L);
  }

  @Test
  void getThisMonthFestivalListTest() {
    // "2023-08-07" ~ "2023-08-10"
    FestivalThumbnailDto thisMonthFestivalList = festivalService.getThisMonthFestivalList(
        memberInfoDto1, 0, 1, Collections.singletonList("우도"), LocalDate.of(2023, 8, 8),
        LocalDate.of(2023, 8, 9));
    Assertions.assertThat(thisMonthFestivalList.getTotalElements()).isSameAs(1L);
  }
}